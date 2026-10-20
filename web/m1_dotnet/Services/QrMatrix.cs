namespace M1.DotNet.Services;

/// <summary>The assignment's custom matrix layout, not an ISO QR encoder.</summary>
public sealed class QrMatrix
{
    public int Size { get; }
    public int[,] Cells { get; }

    public QrMatrix(int size)
    {
        if (size is not (21 or 25)) throw new ArgumentOutOfRangeException(nameof(size));
        Size = size;
        Cells = new int[size, size];
        for (var row = 0; row < size; row++)
            for (var col = 0; col < size; col++) Cells[row, col] = 2;

        Finder(0, 0);
        Finder(0, size - 7);
        Finder(size - 7, 0);

        Horizontal(7, 0, 7, 0);
        Vertical(7, 0, 7, 0);
        Vertical(8, 0, 7, 0);
        Horizontal(7, size - 8, size - 1, 0);
        Vertical(size - 8, 0, 7, 0);
        Horizontal(size - 8, 0, 6, 0);
        Vertical(7, size - 8, size - 1, 0);
        Vertical(8, size - 8, size - 1, 0);

        for (var i = 8; i <= size - 9; i++)
            Cells[6, i] = Cells[i, 6] = i % 2 == 0 ? 1 : 0;

        if (size == 25)
        {
            for (var row = 16; row <= 20; row++)
                for (var col = 16; col <= 20; col++)
                    Cells[row, col] = row is 16 or 20 || col is 16 or 20 ||
                                      (row == 18 && col == 18) ? 1 : 0;
            Vertical(15, 16, 20, 0);
        }
    }

    private void Finder(int top, int left)
    {
        for (var row = 0; row < 7; row++)
            for (var col = 0; col < 7; col++)
                Cells[top + row, left + col] = row is 0 or 6 || col is 0 or 6 ||
                    (row >= 2 && row <= 4 && col >= 2 && col <= 4) ? 1 : 0;
    }

    private void Horizontal(int row, int start, int end, int value)
    {
        for (var col = start; col <= end; col++) Cells[row, col] = value;
    }

    private void Vertical(int col, int start, int end, int value)
    {
        for (var row = start; row <= end; row++) Cells[row, col] = value;
    }

    // Both columns in each pair have matching reserved rows in this layout.
    // Skip timing column 6 and restart downward on the left, as in QREncoding.java.
    public IEnumerable<(int Row, int Col)> DataPositions()
    {
        foreach (var position in Pairs(Size - 1, 7, true)) yield return position;
        foreach (var position in Pairs(5, 0, false)) yield return position;
    }

    private IEnumerable<(int Row, int Col)> Pairs(int end, int start, bool up)
    {
        for (var col = end; col >= start; col -= 2, up = !up)
            for (var step = 0; step < Size; step++)
            {
                var row = up ? Size - 1 - step : step;
                if (Cells[row, col] == 2) yield return (row, col);
                if (Cells[row, col - 1] == 2) yield return (row, col - 1);
            }
    }
}
