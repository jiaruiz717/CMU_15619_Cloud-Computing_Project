using System.Text.Json;
using M2.DotNet.Services;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddSingleton<BlockchainService>();
var app = builder.Build();

app.MapGet("/blockchain", (HttpRequest request, BlockchainService service) =>
{
    const string invalid = BlockchainService.Header + "INVALID";
    try
    {
        return Results.Text(service.Serve(request.Query["cc"].ToString(), request.HttpContext.RequestAborted),
            "text/plain; charset=utf-8");
    }
    catch (Exception error) when (error is FormatException or JsonException or ArgumentException
                                or InvalidDataException or InvalidOperationException or OverflowException)
    {
        return Results.Text(invalid, "text/plain; charset=utf-8");
    }
    catch (TimeoutException)
    {
        return Results.Text(BlockchainService.Header + "MINING_LIMIT_EXCEEDED",
            "text/plain; charset=utf-8", statusCode: 503);
    }
});

app.Run();
