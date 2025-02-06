import json
import matplotlib.pyplot as plt
from matplotlib.dates import date2num
from datetime import date

colors = {
    "SPD": "#E3000F",
    "LINKE": "#BE3075",
    "FW": "#F7A800",
    "CDU/CSU": "#151518",
    "GRÜNE": "#409A3C",
    "BSW": "#7D254F",
    "AfD": "#009EE0",
    "FDP": "#FFED00"
}
start_plot = date2num(date.fromisoformat("2021-01-01"))
end_plot = date2num(date.today())
step_size = 1
smoothing = 7

with open("results.json", "r", encoding="utf-8") as f:
    results = json.load(f)

parties: set[str] = set(results[0]["permille"].keys())
assert parties.issubset(colors.keys())
for result in results:
    assert result["permille"].keys() == parties
    result["date"]["start"] = date2num(date.fromisoformat(result["date"]["start"]))
    result["date"]["end"] = date2num(date.fromisoformat(result["date"]["end"]))

results = [r for r in results if r["date"]["end"] > start_plot - smoothing]
results = sorted(results, key=lambda r: r["date"]["start"])

def get_point(x, party):
    sum = 0
    count = 0
    # This is inefficient
    for r in results:
        s = r["date"]["start"]
        e = r["date"]["end"]
        if e < x - smoothing:
            continue
        if s > x + smoothing:
            break
        if r["permille"][party] < 0:
            continue
        d = (min(e, x + smoothing) - max(s, x - smoothing)) / (e - s + 1)
        sum += 0.1 * d * r["permille"][party]
        count += d

    if count == 0:
        return None
    return x, sum / count

plt.figure(figsize=(19.2, 10.8), dpi=100)

xs = []
x = start_plot
while x <= end_plot:
    xs.append(x)
    x += 0.1
for party in parties:
    points = [get_point(x, party) for x in xs]
    x = [p[0] for p in points if p is not None]
    y = [p[1] for p in points if p is not None]
    plt.plot_date(x, y, c=colors[party], label=party, marker=None, ls='-')
plt.hlines(5, xmin=start_plot, xmax=end_plot, linestyles='--', color="black", label="5%-Hürde")
plt.xlabel("Datum")
plt.ylabel("Umfragewerte in %")
plt.xticks(rotation=90)
plt.legend(loc="upper left")
plt.savefig("results.png")
