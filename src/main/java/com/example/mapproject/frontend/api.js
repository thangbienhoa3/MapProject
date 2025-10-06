// Hàm gọi API backend
async function getRoute(from, to) {
    const res = await fetch("http://localhost:8080/api/route", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ from, to })
    });
    if (!res.ok) throw new Error("API error " + res.status);
    return await res.json();
}

async function sendFloodedPolygon(floodCoords) {
    const res = await fetch("http://localhost:8080/api/flooded", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(floodCoords)
    });

    if (!res.ok) throw new Error("API error " + res.status);
    return await res.text();
}