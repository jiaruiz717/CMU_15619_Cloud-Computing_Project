using M1.DotNet.Services;

var builder = WebApplication.CreateBuilder(args);
builder.Services.AddSingleton<QrCodeService>();
var app = builder.Build();

app.MapGet("/qrcode", (HttpRequest request, QrCodeService qr) =>
{
    var type = request.Query["type"].ToString();
    var data = request.Query["data"].ToString();
    try
    {
        var result = type switch
        {
            "encode" => qr.Encode(data),
            "decode" => qr.Decode(data),
            _ => "No such type!"
        };
        return Results.Text(result, "text/plain; charset=utf-8");
    }
    catch (ArgumentException)
    {
        return Results.Text("", "text/plain; charset=utf-8", statusCode: 400);
    }
    catch (FormatException)
    {
        return Results.Text("", "text/plain; charset=utf-8", statusCode: 400);
    }
});

app.Run();
