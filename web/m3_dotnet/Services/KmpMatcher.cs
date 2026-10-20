namespace M3.DotNet.Services;

public static class KmpMatcher
{
    /// <summary>Case-sensitive substring count including overlaps, e.g. aa in aaa = 2.</summary>
    public static int CountOccurrences(string text, string pattern)
    {
        if (pattern.Length == 0) return 0;
        var prefix = new int[pattern.Length];
        for (int i = 1, matched = 0; i < pattern.Length; i++)
        {
            while (matched > 0 && pattern[i] != pattern[matched]) matched = prefix[matched - 1];
            if (pattern[i] == pattern[matched]) matched++;
            prefix[i] = matched;
        }
        int count = 0, length = 0;
        foreach (var character in text)
        {
            while (length > 0 && character != pattern[length]) length = prefix[length - 1];
            if (character == pattern[length]) length++;
            if (length != pattern.Length) continue;
            count++;
            length = prefix[length - 1];
        }
        return count;
    }
}
