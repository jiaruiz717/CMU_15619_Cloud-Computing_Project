using System.Globalization;
using System.Numerics;
using System.Text.Json.Nodes;
using static M2.DotNet.Services.ChainProtocol;

namespace M2.DotNet.Services;

public sealed class BlockchainService(IConfiguration configuration)
{
    public const string Header = "dwendwen,144331934093\n";
    private readonly long _maxAttempts = configuration.GetValue<long?>("Mining:MaxAttempts") ?? 50_000_000;

    public string Serve(string encoded, CancellationToken cancellationToken)
    {
        var request = Object(JsonNode.Parse(PayloadCodec.Decode(encoded)));
        if (!new ChainValidator().Verify(request)) return Header + "INVALID";
        var chain = Array(request, "chain");
        chain.Add(Mine(request, cancellationToken));
        // Nodes have one parent; use a clone when building the response envelope.
        return Header + PayloadCodec.Encode(new JsonObject { ["chain"] = chain.DeepClone() }.ToJsonString());
    }

    private JsonObject Mine(JsonObject request, CancellationToken cancellationToken)
    {
        var chain = Array(request, "chain");
        var transactions = (JsonArray)Array(request, "new_tx").DeepClone();
        foreach (var node in transactions)
        {
            var transaction = Object(node);
            if (transaction.Count == 3) ChainValidator.CompleteTransaction(transaction);
        }
        BigInteger previousRewardTime = 0;
        var previousHash = "00000000";
        if (chain.Count > 0)
        {
            var previousBlock = Object(chain[chain.Count - 1]);
            var previousTransactions = Array(previousBlock, "all_tx");
            previousRewardTime = Integer(Object(previousTransactions[previousTransactions.Count - 1]), "time");
            previousHash = Text(previousBlock, "hash");
        }
        var reward = new JsonObject
        {
            ["amt"] = Number(Reward(chain.Count)),
            ["recv"] = Number(BigInteger.Parse(TeamAccount)),
            ["time"] = (previousRewardTime + BlockInterval).ToString(CultureInfo.InvariantCulture)
        };
        reward["hash"] = TransactionHash(reward);
        transactions.Add(reward);

        var target = Text(request, "new_target");
        var prefix = BlockPrefix(chain.Count, previousHash, transactions);
        for (long nonce = 0; nonce < _maxAttempts; nonce++)
        {
            if (nonce % 4096 == 0) cancellationToken.ThrowIfCancellationRequested();
            var pow = nonce.ToString(CultureInfo.InvariantCulture);
            var hash = Sha256(prefix + pow)[..8];
            if (string.CompareOrdinal(hash, target) < 0)
                return new JsonObject
                {
                    ["id"] = chain.Count, ["all_tx"] = transactions,
                    ["target"] = target, ["hash"] = hash, ["pow"] = pow
                };
        }
        throw new TimeoutException("No proof of work found within Mining:MaxAttempts.");
    }
}
