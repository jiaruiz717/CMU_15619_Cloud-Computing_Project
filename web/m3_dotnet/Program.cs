using System.Globalization;
using System.Text.Json;
using M3.DotNet.Data;
using M3.DotNet.Services;
using MySqlConnector;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddSingleton<ContactRepository>();
builder.Services.AddSingleton<TwitterService>();
var app = builder.Build();

app.MapGet("/", () => Results.Text("Welcome!"));
app.MapGet("/twitter", async (HttpRequest request, TwitterService service, ILogger<TwitterService> logger) =>
{
    var query = request.Query;
    var type = query["type"].ToString();
    var phrase = query["phrase"].ToString();
    var hashtag = query["hashtag"].ToString();
    if (!long.TryParse(query["user_id"].ToString(), NumberStyles.None, CultureInfo.InvariantCulture, out var id)
        || id <= 0 || type is not ("reply" or "retweet" or "both")
        || string.IsNullOrEmpty(phrase) || string.IsNullOrEmpty(hashtag))
        return Results.Text(TwitterService.Header + "INVALID", "text/plain; charset=utf-8");
    try
    {
        return Results.Text(await service.ServeAsync(id, type, phrase, hashtag,
            request.HttpContext.RequestAborted), "text/plain; charset=utf-8");
    }
    catch (Exception error) when (error is MySqlException or JsonException or InvalidDataException)
    {
        logger.LogError(error, "Unable to load Twitter contacts for user {UserId}", id);
        return Results.Text(TwitterService.Header + "ERROR", "text/plain; charset=utf-8", statusCode: 503);
    }
});

app.Run();
