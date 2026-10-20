using System.Globalization;
using System.Text;

namespace M1.DotNet.Services;

public sealed class QrCodeService
{
    private const string Padding = "1110110000010001";

    public string Encode(string text)
    {
        if (text.Length is < 1 or > 22 || text.Any(c => c > 255))
            throw new ArgumentException("Use 1–22 single-byte characters.", nameof(text));

        var template = new QrMatrix(text.Length <= 13 ? 21 : 25);
        var bits = new StringBuilder(ByteBits(text.Length));
        foreach (var character in text)
        {
            var valueBits = ByteBits(character);
            bits.Append(valueBits);
            bits.Append(ByteBits(valueBits.Count(c => c == '1') % 2));
        }
        while (bits.Length < 625) bits.Append(Padding);
        // Materialize before filling: the iterator uses 2 to identify empty cells.
        var positions = template.DataPositions().ToArray();
        for (var i = 0; i < positions.Length; i++)
            template.Cells[positions[i].Row, positions[i].Col] = bits[i] - '0';

        var rowMajor = new StringBuilder();
        foreach (var cell in template.Cells) rowMajor.Append(cell);
        var encrypted = LogisticMap.Transform(rowMajor.ToString());
        var result = new StringBuilder();
        for (var i = 0; i < encrypted.Length; i += 32)
        {
            var chunk = encrypted.Substring(i, Math.Min(32, encrypted.Length - i));
            result.Append("0x").Append(Convert.ToUInt32(chunk, 2).ToString("x", CultureInfo.InvariantCulture));
        }
        return result.ToString();
    }

    public string Decode(string hex)
    {
        // The original decoder accepts a 32x32 encrypted canvas, not Encode's compact matrix.
        var chunks = hex.Split("0x", StringSplitOptions.None);
        if (chunks.Length != 33 || chunks[0] != "")
            throw new FormatException("Expected 32 hexadecimal words prefixed by 0x.");
        var binary = new StringBuilder(1024);
        foreach (var chunk in chunks.Skip(1))
        {
            if (chunk.Length is < 1 or > 8 || !chunk.All(Uri.IsHexDigit))
                throw new FormatException("Invalid hexadecimal word.");
            var word = uint.Parse(chunk, NumberStyles.HexNumber, CultureInfo.InvariantCulture);
            binary.Append(Convert.ToString((long)word, 2).PadLeft(32, '0'));
        }
        var clear = LogisticMap.Transform(binary.ToString());
        var canvas = new int[32, 32];
        for (var i = 0; i < clear.Length; i++) canvas[i / 32, i % 32] = clear[i] - '0';

        // Try four orientations and all legal placements of a 21x21 or 25x25 image.
        for (var rotation = 0; rotation < 4; rotation++, canvas = Rotate(canvas))
            foreach (var size in new[] { 21, 25 })
                for (var top = 0; top <= 32 - size; top++)
                    for (var left = 0; left <= 32 - size; left++)
                    {
                        if (!IsFinder(canvas, top, left) ||
                            !IsFinder(canvas, top, left + size - 7) ||
                            !IsFinder(canvas, top + size - 7, left)) continue;
                        var result = ReadText(canvas, top, left, size);
                        if (result is not null) return result;
                    }
        throw new FormatException("No valid custom QR matrix found.");
    }

    private static string? ReadText(int[,] canvas, int top, int left, int size)
    {
        var bits = new string(new QrMatrix(size).DataPositions()
            .Select(p => (char)('0' + canvas[top + p.Row, left + p.Col])).ToArray());
        var length = Convert.ToInt32(bits[..8], 2);
        if (length < 1 || length > (size == 21 ? 13 : 22) || 8 + length * 16 > bits.Length)
            return null;
        var text = new StringBuilder(length);
        for (var i = 0; i < length; i++)
            // The source decoder skips the parity byte without correcting errors.
            text.Append((char)Convert.ToInt32(bits.Substring(8 + i * 16, 8), 2));
        return text.ToString();
    }

    private static bool IsFinder(int[,] canvas, int top, int left)
    {
        for (var row = 0; row < 7; row++)
            for (var col = 0; col < 7; col++)
            {
                var expected = row is 0 or 6 || col is 0 or 6 ||
                    (row >= 2 && row <= 4 && col >= 2 && col <= 4) ? 1 : 0;
                if (canvas[top + row, left + col] != expected) return false;
            }
        return true;
    }

    private static int[,] Rotate(int[,] matrix)
    {
        var rotated = new int[32, 32];
        for (var row = 0; row < 32; row++)
            for (var col = 0; col < 32; col++) rotated[col, 31 - row] = matrix[row, col];
        return rotated;
    }

    private static string ByteBits(int value) => Convert.ToString(value, 2).PadLeft(8, '0');
}
