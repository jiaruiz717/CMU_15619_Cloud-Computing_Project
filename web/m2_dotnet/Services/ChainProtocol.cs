using System.Globalization;
using System.Numerics;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json.Nodes;

namespace M2.DotNet.Services;

/// <summary>Assignment protocol constants. These small RSA keys are not production cryptography.</summary>
public static class ChainProtocol
{
    public const string TeamAccount = "1097844002039";
    public static readonly BigInteger Modulus = BigInteger.Parse("1561906343821");
    private static readonly BigInteger PrivateExponent = BigInteger.Parse("343710770439");
    public static readonly BigInteger BlockInterval = BigInteger.Parse("600000000000");

    public static string Text(JsonObject value, string key) =>
        value[key]?.ToString() ?? throw new FormatException($"Missing {key}.");

    public static BigInteger Integer(JsonObject value, string key) =>
        BigInteger.Parse(Text(value, key), NumberStyles.AllowLeadingSign, CultureInfo.InvariantCulture);

    public static JsonObject Object(JsonNode? value) => value as JsonObject
        ?? throw new FormatException("Expected a JSON object.");

    public static JsonArray Array(JsonObject value, string key) => value[key] as JsonArray
        ?? throw new FormatException($"Expected an array: {key}.");

    public static JsonNode Number(BigInteger number) =>
        JsonNode.Parse(number.ToString(CultureInfo.InvariantCulture))!;

    public static string Sha256(string value) =>
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(value))).ToLowerInvariant();

    public static string TransactionHash(JsonObject transaction) =>
        Sha256(string.Join("|", new[] { "time", "send", "recv", "amt", "fee" }
            .Select(key => transaction[key]?.ToString() ?? "")))[..8];

    public static string BlockPrefix(int id, string previousHash, JsonArray transactions) =>
        Sha256(id.ToString(CultureInfo.InvariantCulture) + "|" + previousHash +
            string.Concat(transactions.Select(tx => "|" + Text(Object(tx), "hash"))));

    public static bool IsHash(string value) => value.Length == 8 &&
        value.All(c => c is >= '0' and <= '9' or >= 'a' and <= 'f');

    public static BigInteger HashNumber(string hash) =>
        BigInteger.Parse("0" + hash, NumberStyles.AllowHexSpecifier, CultureInfo.InvariantCulture);

    public static BigInteger Sign(string hash) => BigInteger.ModPow(HashNumber(hash), PrivateExponent, Modulus);
    public static BigInteger Reward(int id) => new BigInteger(500_000_000) >> (id / 2);

    public static bool HasExactly(JsonObject value, params string[] keys) =>
        value.Count == keys.Length && keys.All(key => value[key] is not null);
}
