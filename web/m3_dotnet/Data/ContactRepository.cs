using System.Text.Json;
using System.Text.Json.Serialization;
using M3.DotNet.Models;
using MySqlConnector;

namespace M3.DotNet.Data;

public sealed class ContactRepository
{
    private readonly string _connectionString;
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        NumberHandling = JsonNumberHandling.AllowReadingFromString
    };

    public ContactRepository(IConfiguration configuration)
    {
        var configured = configuration.GetConnectionString("Twitter");
        _connectionString = !string.IsNullOrWhiteSpace(configured) ? configured :
            new MySqlConnectionStringBuilder
            {
                Server = configuration["mysqlHost"] ?? "localhost",
                Port = 3306,
                Database = "twitter_db",
                UserID = configuration["mysqlUser"] ?? "root",
                Password = configuration["mysqlPassword"] ?? "",
                Pooling = true,
                ConnectionTimeout = 10,
                DefaultCommandTimeout = 30
            }.ConnectionString;
    }

    public async Task<IReadOnlyList<ContactRow>> GetContactsAsync(long userId, CancellationToken cancellationToken)
    {
        // Per-request pooled connections avoid sharing a non-thread-safe global SQL connection.
        await using var connection = new MySqlConnection(_connectionString);
        await connection.OpenAsync(cancellationToken);
        await using var command = connection.CreateCommand();
        command.CommandText = """
            SELECT is_reply, is_retweet, contact_tweet
            FROM contact_tweets
            WHERE user_id = @userId
            """;
        command.Parameters.AddWithValue("@userId", userId);
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        var rows = new List<ContactRow>();
        while (await reader.ReadAsync(cancellationToken))
        {
            var contacts = JsonSerializer.Deserialize<List<ContactTweet>>(reader.GetString(2), JsonOptions)
                ?? throw new InvalidDataException("Expected a contact_tweet JSON array.");
            foreach (var contact in contacts)
                if (contact.OtherUserId <= 0 || contact.AllTexts is null || contact.AllHashtags is null ||
                    contact.LatestTweetText is null || !double.IsFinite(contact.InteractionScore) ||
                    contact.InteractionScore < 0)
                    throw new InvalidDataException("Invalid contact tweet record.");
            rows.Add(new ContactRow(reader.GetBoolean(0), reader.GetBoolean(1), contacts));
        }
        return rows;
    }
}
