// Khởi tạo bản đồ
const map = L.map('map').setView([21.028, 105.85], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
}).addTo(map);

let from = null;
let to = null;
let polyline = null;
let markers = [];

// Sự kiện click trên bản đồ
map.on('click', async function (e) {
    const { lat, lng } = e.latlng;

    if (!from) {
        from = { lat, lng };
        markers.push(L.marker([lat, lng]).addTo(map).bindPopup("Điểm đầu").openPopup());
    } else if (!to) {
        to = { lat, lng };
        markers.push(L.marker([lat, lng]).addTo(map).bindPopup("Điểm cuối").openPopup());

        try {
            const data = await getRoute(from, to);
            log("API response: " + JSON.stringify(data));

            if (!data.path || data.path.length === 0) {
                alert("API không trả về path!");
                return;
            }

            const path = data.path.map(p => [p.lat, p.lng]);

            if (polyline) map.removeLayer(polyline);

            polyline = L.polyline(path, { color: "red", weight: 4 }).addTo(map);
            map.fitBounds(path);
        } catch (err) {
            console.error(err);
            alert("Không lấy được tuyến đường từ API");
        }
    } else {
        alert("Bạn đã chọn đủ 2 điểm. Nhấn nút Refresh để chọn lại.");
    }
});
document.addEventListener("keydown", function(event) {
    if (event.key === "F5") {
        event.preventDefault();
        resetMap();   // reset bản đồ thay vì reload hẳn trang
    }
});


let drawingFlood = false;
let floodCoords = [];
let floodPolyline = null;

map.on('mousedown', function(e) {
    if (e.originalEvent.shiftKey) { // chỉ vẽ khi giữ Shift
        drawingFlood = true;
        floodCoords = [e.latlng];
        floodPolyline = L.polyline(floodCoords, { color: "blue", weight: 6 }).addTo(map);
    }
});

map.on('mousemove', function(e) {
    if (drawingFlood) {
        floodCoords.push(e.latlng);
        floodPolyline.setLatLngs(floodCoords);
    }
});

map.on('mouseup', async function (e) {
    if (drawingFlood) {
        drawingFlood = false;
        console.log("Flooded segment:", floodCoords);
        await sendFloodedPolygon(floodCoords)
    }
});

function resetMap() {
    markers.forEach(m => map.removeLayer(m));
    markers = [];
    if (polyline) { map.removeLayer(polyline); polyline = null; }

    from = null; to = null;
    alert("Đã reset, hãy chọn lại điểm Start và End.");
}
