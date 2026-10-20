using System.Text.Json.Serialization;

namespace M3.DotNet.Models;

/// <summary>Shape of each item in the existing contact_tweets.contact_tweet JSON array.</summary>
public sealed class ContactTweet
{
    [JsonPropertyName("other_user_id")]
    public required long OtherUserId { get; init; }

    [JsonPropertyName("all_texts")]
    public required string AllTexts { get; init; }

    [JsonPropertyName("maxtid")]
    public required long LatestTweetId { get; init; }

    [JsonPropertyName("maxtid_text")]
    public required string LatestTweetText { get; init; }

    [JsonPropertyName("all_hashtags")]
    public required string AllHashtags { get; init; }

    [JsonPropertyName("hashtag_count")]
    public int? HashtagCount { get; init; }

    [JsonPropertyName("interaction_score")]
    public required double InteractionScore { get; init; }

    [JsonPropertyName("other_screen_name")]
    public string? ScreenName { get; init; }

    [JsonPropertyName("other_description")]
    public string? Description { get; init; }
}

public sealed record ContactRow(bool IsReply, bool IsRetweet, IReadOnlyList<ContactTweet> Contacts);
