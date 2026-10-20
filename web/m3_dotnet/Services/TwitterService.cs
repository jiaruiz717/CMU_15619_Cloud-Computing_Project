using System.Globalization;
using M3.DotNet.Data;
using M3.DotNet.Models;

namespace M3.DotNet.Services;

public sealed class TwitterService(ContactRepository repository)
{
    public const string Header = "dwendwen,144331934093\n";

    public async Task<string> ServeAsync(long userId, string type, string phrase, string hashtag,
        CancellationToken cancellationToken)
    {
        var rows = await repository.GetContactsAsync(userId, cancellationToken);
        var replies = new Dictionary<long, RankedContact>();
        var retweets = new Dictionary<long, RankedContact>();
        foreach (var row in rows)
        {
            // Preserve the original classification: every row other than reply-only is retweet.
            var destination = row.IsReply && !row.IsRetweet ? replies : retweets;
            foreach (var contact in row.Contacts)
                destination[contact.OtherUserId] = new RankedContact(contact, userId);
        }
        if (replies.Count == 0 && retweets.Count == 0) return Header + "INVALID";
        Dictionary<long, RankedContact> selected;
        switch (type)
        {
            case "reply": selected = replies; break;
            case "retweet": selected = retweets; break;
            case "both":
                selected = replies;
                foreach (var (id, contact) in retweets)
                    if (selected.TryGetValue(id, out var existing)) existing.Merge(contact);
                    else selected.Add(id, contact);
                break;
            default: return Header + "INVALID";
        }

        foreach (var contact in selected.Values)
        {
            long matches = contact.Hashtags.LongCount(tag =>
                string.Equals(tag, hashtag, StringComparison.OrdinalIgnoreCase));
            foreach (var text in contact.Texts) matches += KmpMatcher.CountOccurrences(text, phrase);
            var keywordScore = 1 + Math.Log(matches + 1d);
            contact.Score = contact.InteractionScore * contact.HashtagScore * keywordScore;
            if (!double.IsFinite(contact.Score)) throw new InvalidDataException("Non-finite contact score.");
        }
        var lines = selected.Values.Where(contact => contact.Score > 0)
            .OrderByDescending(contact => contact.Score)
            .ThenByDescending(contact => contact.Id)
            .Select(contact => string.Join("\t", contact.Id.ToString(CultureInfo.InvariantCulture),
                contact.ScreenName, contact.Description, contact.LatestTweetText));
        return Header + string.Join("\n", lines);
    }
}
