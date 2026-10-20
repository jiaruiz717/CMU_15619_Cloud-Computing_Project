namespace M3.DotNet.Models;

public sealed class RankedContact
{
    public long Id { get; init; }
    public long LatestTweetId { get; private set; }
    public string LatestTweetText { get; private set; }
    public string ScreenName { get; }
    public string Description { get; }
    public double InteractionScore { get; }
    public double HashtagScore { get; }
    public double Score { get; set; }
    public List<string> Texts { get; } = [];
    public List<string> Hashtags { get; } = [];

    public RankedContact(ContactTweet contact, long queryUserId)
    {
        Id = contact.OtherUserId;
        LatestTweetId = contact.LatestTweetId;
        LatestTweetText = contact.LatestTweetText;
        ScreenName = contact.ScreenName ?? "";
        Description = contact.Description ?? "";
        InteractionScore = contact.InteractionScore;
        var count = contact.HashtagCount ?? 0;
        HashtagScore = count <= 10 || Id == queryUserId ? 1 : 1 + Math.Log(1d + count - 10);
        Texts.AddRange(contact.AllTexts.Split('\u001f', StringSplitOptions.RemoveEmptyEntries));
        Hashtags.AddRange(contact.AllHashtags.Split('\u001f', StringSplitOptions.RemoveEmptyEntries));
    }

    public void Merge(RankedContact other)
    {
        Texts.AddRange(other.Texts);
        Hashtags.AddRange(other.Hashtags);
        if (other.LatestTweetId > LatestTweetId)
        {
            LatestTweetId = other.LatestTweetId;
            LatestTweetText = other.LatestTweetText;
        }
        // Scores and profile remain those from the reply row, matching Data.java's merge.
    }
}
