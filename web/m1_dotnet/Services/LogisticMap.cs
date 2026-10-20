namespace M1.DotNet.Services;

public static class LogisticMap
{
    /// <summary>XOR row-major bits with least-significant bits of each logistic byte.</summary>
    public static string Transform(string bits)
    {
        var output = bits.ToCharArray();
        double x = 0.1;
        for (var offset = 0; offset < bits.Length; offset += 8)
        {
            var value = (int)Math.Floor(x * 255);
            for (var bit = 0; bit < 8 && offset + bit < bits.Length; bit++)
                output[offset + bit] = (char)('0' + ((bits[offset + bit] - '0') ^ ((value >> bit) & 1)));
            // Preserve the Java operation order; this chaotic sequence is precision-sensitive.
            x = 4 * x * (1 - x);
        }
        return new string(output);
    }
}
