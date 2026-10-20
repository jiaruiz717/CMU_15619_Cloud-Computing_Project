using System.IO.Compression;
using System.Text;

namespace M2.DotNet.Services;

public static class PayloadCodec
{
    private const int MaximumBytes = 4 * 1024 * 1024;

    public static string Decode(string encoded)
    {
        if (encoded.Length == 0 || encoded.Length > MaximumBytes * 2)
            throw new FormatException("Invalid encoded payload length.");
        var base64 = encoded.Replace('-', '+').Replace('_', '/');
        base64 = base64.PadRight((base64.Length + 3) / 4 * 4, '=');
        using var input = new MemoryStream(Convert.FromBase64String(base64));
        // Java Inflater/Deflater use the zlib wrapper, not a raw DeflateStream or gzip.
        using var zlib = new ZLibStream(input, CompressionMode.Decompress);
        using var output = new MemoryStream();
        var buffer = new byte[8192];
        int count;
        while ((count = zlib.Read(buffer)) > 0)
        {
            if (output.Length + count > MaximumBytes)
                throw new InvalidDataException("Decompressed payload exceeds 4 MiB.");
            output.Write(buffer, 0, count);
        }
        return new UTF8Encoding(false, true).GetString(output.ToArray());
    }

    public static string Encode(string json)
    {
        using var output = new MemoryStream();
        using (var zlib = new ZLibStream(output, CompressionLevel.Optimal, leaveOpen: true))
            zlib.Write(Encoding.UTF8.GetBytes(json));
        return Convert.ToBase64String(output.ToArray()).Replace('+', '-').Replace('/', '_');
    }
}
