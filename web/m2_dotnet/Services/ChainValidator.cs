using System.Numerics;
using System.Text.Json.Nodes;
using static M2.DotNet.Services.ChainProtocol;

namespace M2.DotNet.Services;

public sealed class ChainValidator
{
    public bool Verify(JsonObject request)
    {
        if (!HasExactly(request, "chain", "new_tx", "new_target")) return false;
        var target = Text(request, "new_target");
        if (!IsHash(target) || target == "00000000") return false;
        var chain = Array(request, "chain");
        var pending = Array(request, "new_tx");
        var previousHash = "00000000";
        BigInteger previousTime = 0;
        var balances = new Dictionary<string, BigInteger>(StringComparer.Ordinal);

        for (var id = 0; id < chain.Count; id++)
        {
            var block = Object(chain[id]);
            if (!VerifyBlock(block, id, previousHash)) return false;
            var transactions = Array(block, "all_tx");
            // A block must contain its final reward transaction.
            if (transactions.Count == 0) return false;
            for (var i = 0; i < transactions.Count; i++)
            {
                var transaction = Object(transactions[i]);
                if (!VerifyTransaction(transaction, id, previousTime, balances,
                        reward: i == transactions.Count - 1)) return false;
                previousTime = Integer(transaction, "time");
            }
            previousHash = Text(block, "hash");
        }

        var rewardTime = previousTime + BlockInterval;
        foreach (var node in pending)
        {
            // Validate completed copies so signatures, balances and hashes share one code path.
            var transaction = (JsonObject)Object(node).DeepClone();
            if (transaction.Count == 3)
            {
                if (!HasExactly(transaction, "amt", "recv", "time")) return false;
                CompleteTransaction(transaction);
            }
            if (!VerifyTransaction(transaction, chain.Count, previousTime, balances, reward: false))
                return false;
            previousTime = Integer(transaction, "time");
        }
        // Do not create a chain whose final reward breaks chronological ordering.
        return previousTime < rewardTime;
    }

    private static bool VerifyBlock(JsonObject block, int id, string previousHash)
    {
        if (!new[] { "all_tx", "hash", "id", "pow", "target" }.All(block.ContainsKey) ||
            Integer(block, "id") != id) return false;
        var target = Text(block, "target");
        if (!IsHash(target) || Integer(block, "pow") < 0) return false;
        var prefix = BlockPrefix(id, previousHash, Array(block, "all_tx"));
        var computed = Sha256(prefix + Text(block, "pow"))[..8];
        return computed == Text(block, "hash") && string.CompareOrdinal(computed, target) < 0;
    }

    private static bool VerifyTransaction(JsonObject tx, int id, BigInteger previousTime,
        Dictionary<string, BigInteger> balances, bool reward)
    {
        if (reward ? !HasExactly(tx, "amt", "hash", "recv", "time") :
            !HasExactly(tx, "amt", "hash", "fee", "recv", "send", "sig", "time")) return false;
        var amount = Integer(tx, "amt");
        var receiver = Text(tx, "recv");
        if (amount < 0 || Integer(tx, "recv") <= 0 || Integer(tx, "time") <= previousTime ||
            TransactionHash(tx) != Text(tx, "hash")) return false;
        if (reward)
        {
            if (amount != Reward(id)) return false;
        }
        else
        {
            var fee = Integer(tx, "fee");
            var sender = Text(tx, "send");
            var exponent = Integer(tx, "send");
            var signature = Integer(tx, "sig");
            if (fee < 0 || exponent <= 0 || signature < 0 || signature >= Modulus ||
                BigInteger.ModPow(signature, exponent, Modulus) != HashNumber(Text(tx, "hash")))
                return false;
            var balance = balances.GetValueOrDefault(sender) - amount - fee;
            if (balance < 0) return false;
            balances[sender] = balance;
        }
        balances[receiver] = balances.GetValueOrDefault(receiver) + amount;
        return true;
    }

    public static void CompleteTransaction(JsonObject tx)
    {
        tx["send"] = Number(BigInteger.Parse(TeamAccount));
        tx["fee"] = 0;
        var hash = TransactionHash(tx);
        tx["hash"] = hash;
        tx["sig"] = Number(Sign(hash));
    }
}
