# CinemaSeatMap

Đồ án giữa kỳ Phát triển phần mềm cho thiết bị di động.

## Phạm vi đã chốt

- Android native: Java + XML, Custom View + Canvas.
- Một phòng chiếu, đúng 200 đối tượng ghế trên một Canvas.
- Ghế thường: 120 đối tượng (A–F, 20 ghế/hàng).
- Ghế VIP: 60 đối tượng (G–I, 20 ghế/hàng).
- Ghế đôi: 20 đối tượng (J–K, 10 cặp/hàng); một cặp là một đối tượng.
- Trạng thái: trống, đã đặt, bị khóa và đang chọn.
- Pan, pinch-to-zoom, hit-testing, chú thích, lọc loại ghế và Reset View.
- Bottom Sheet thu gọn/mở rộng, đồng bộ hai chiều với Canvas.
- Giữ lựa chọn khi xoay màn hình; dữ liệu giả lập, không cần backend.

## Tiến độ

- [x] Tạo project Empty Views Activity, Java, min SDK 24.
- [x] Thiết kế dữ liệu và vẽ sơ đồ 200 ghế.
- [x] Pan, zoom, hit-testing; kiểm thử pinch bằng MotionEvent hai ngón trên máy ảo.
- [x] Bộ lọc, chú thích và Reset View.
- [x] Bottom Sheet và quản lý trạng thái.
- [ ] Kiểm thử dọc/ngang, hiệu năng và trường hợp biên.
- [ ] Hoàn thiện tài liệu và video 1–5 phút.

## Chạy dự án

Mở thư mục repository trong Android Studio, đợi Gradle Sync, chọn thiết bị Android API 24 trở lên và Run app.

## Kiến trúc dự kiến

Dữ liệu ghế và dữ liệu giả lập tách khỏi ViewModel quản lý trạng thái và SeatMapView đảm nhiệm vẽ/tương tác. MainActivity kết nối giao diện XML với trạng thái chung.

Công thức tọa độ, hit-testing, quản lý trạng thái và tối ưu hiệu năng sẽ được bổ sung theo phần triển khai thực tế.

## Video

Sẽ bổ sung khi hoàn thiện.

## Nhật ký sử dụng AI

| Ngày | Agent | Nhiệm vụ / phần thay đổi | Kiểm tra và hạn chế |
|---|---|---|---|
| 25/11/2026 | Codex | Phân tích đề, đề xuất quy trình và phạm vi 200 đối tượng; tạo project Java + XML bằng Android Studio | Đối chiếu yêu cầu và barem; hiện là khung project, chưa có chức năng chọn ghế. Sinh viên cần xem và hiểu mã trước khi nộp. |

## Xác minh project nền

- Đã Gradle Sync thành công trong Android Studio.
- Đã build debug thành công và cài/chạy trên Medium Phone API 36; màn hình mặc định hiển thị Hello World.
- Thư mục dự án có dấu tiếng Việt; áp dụng android.overridePathCheck=true theo hướng dẫn lỗi của Android Gradle Plugin. Cần tiếp tục kiểm tra build trong các mốc phát triển sau.
- Nhật ký AI bổ sung: Codex xử lý các hộp thoại thêm file Git, xác định lỗi đường dẫn non-ASCII, bổ sung cấu hình Gradle và kiểm chứng build/chạy bằng giao diện Android Studio. Sau đó đã triển khai sơ đồ tĩnh như ghi nhận bên dưới.


## Mốc sơ đồ tĩnh 200 đối tượng

- `model/Seat.java`: dữ liệu bất biến, loại ghế, trạng thái, giá và hình học tọa độ world.
- `data/MockSeatRepository.java`: tạo danh sách cố định 200 đối tượng; 120 thường, 60 VIP, 20 ghế đôi. Một ghế đôi tương ứng hai chỗ, tổng cộng 220 chỗ ngồi.
- `ui/SeatMapView.java`: một Canvas vẽ toàn bộ ghế, màn hình chiếu, nhãn phân khu và hàng; tái sử dụng Paint và RectF; tự fit sơ đồ khi kích thước View thay đổi.
- `layout/activity_main.xml` và `layout-land/activity_main.xml`: khung XML cho dọc/ngang, chú thích ba trạng thái và ký hiệu loại ghế.
- Xác minh dữ liệu bằng Java: đúng 200 ID không trùng, phân bổ 120/60/20, đủ ba trạng thái, không chồng lấn và không vượt biên world.
- Build debug thành công trong Android Studio, đã chạy trên Medium Phone API 36 và xoay dọc/ngang để kiểm tra bố cục.
- Nhật ký AI: Codex sinh/chỉnh sửa các file nêu trên, kiểm tra dữ liệu và build/chạy; phát hiện sơ đồ quá nhỏ ở chế độ ngang, bổ sung layout ngang với cột thông tin bên trái. Sinh viên cần tự xem và giải thích mã nguồn. Chưa có Pan/Zoom, chọn ghế, bộ lọc hoặc Bottom Sheet.

## Mốc thiết kế giao diện

- Nền chuyển sắc tối, tiêu đề và thông tin phòng chiếu, bảng giá theo loại ghế.
- Canvas vẽ màn chiếu cong và ánh sáng nhẹ, ghế có lưng tựa/đệm/tay vịn; thường xanh ngọc, VIP vàng, ghế đôi tím. Ghế đã đặt xám, ghế khóa có dấu chéo.
- Paint, RectF, Path, Shader và Typeface được tạo/tái sử dụng ngoài onDraw; hình học 200 đối tượng được giữ nguyên.
- Thanh trạng thái dùng biểu tượng sáng trên nền tối; layout ngang có cột thông tin cuộn.
- Nhật ký AI: Codex chỉnh SeatMapView, MainActivity, XML dọc/ngang, strings và drawable; kiểm tra build/cài/chạy thành công trên Medium Phone API 36, review màn hình dọc/ngang và sửa xuống dòng bảng giá/độ tương phản thanh trạng thái. Pan/Zoom và chọn ghế vẫn là mốc tiếp theo.

### Điều chỉnh theo ảnh mẫu tím hồng

Codex áp dụng bảng màu tím mận/rose thống nhất cho nền, ghế, chữ, bảng giá và ánh sáng màn chiếu. Ghế trống cùng tông hồng, đã đặt tím trầm, khóa có dấu chéo; VIP dùng viền sáng và nhãn phân khu, ghế đôi dùng hình cặp. Hiệu ứng hạt sáng có tọa độ tạo cố định ngoài onDraw. Đã build/cài/chạy thành công và xem cả dọc/ngang trên Medium Phone API 36. Dữ liệu và hình học 200 đối tượng không đổi.


## Mốc tương tác và sắc độ ghế

- Ghế thường hồng đậm, VIP hồng vừa, ghế đôi hồng nhạt. Đã đặt xám tím, khóa tối có dấu chéo, đang chọn hồng sáng có dấu tick và viền.
- `MapTransform`: screen = world * scale + translation; world = (screen - translation) / scale. Zoom giữ world-point dưới tâm cử chỉ; tỷ lệ giới hạn từ fit đến 5 lần fit. Pan kẹp biên theo kích thước nội dung và viewport.
- `SeatMapView`: ScaleGestureDetector xử lý pinch; kéo vượt touch slop mới pan; cử chỉ nhiều ngón/drag/cancel không tạo tap chọn nhầm. Tap đổi sang world rồi kiểm tra bounds từng ghế, gồm cả biên. Nút +/− là công cụ hỗ trợ zoom, không thay thế pinch.
- `SeatSelectionViewModel`: nguồn lựa chọn thống nhất, chỉ cho chọn ghế trống; tổng giá cập nhật ngay. ViewModel giữ lựa chọn khi xoay, Bundle lưu ID để phục hồi Activity. Ghế đôi có một ID và giá cho cả cặp.
- Reset View nội suy vị trí/tỷ lệ trong 280ms, giữ danh sách ghế đã chọn. Thay đổi kích thước đưa góc nhìn về fit, giữ lựa chọn.
- `MapTransformTest`: kiểm tra tâm zoom, min/max, biên pan, phép đổi tọa độ tại điểm gần biên của 200 ghế và reset. Đã chạy cùng hai phương thức kiểm thử bằng Java độc lập (assertions), chưa chạy qua Gradle JUnit runner.
- Kiểm tra UI trên Medium Phone API 36: chọn A2, zoom bằng nút +, kéo, bỏ chọn A2 đúng sau biến đổi, chọn A3, Reset View giữ A3/80.000đ, xoay ngang rồi dọc vẫn giữ A3. Build và cài/chạy thành công. Cử chỉ pinch hai ngón đã triển khai, chưa kiểm chứng trực tiếp bằng cảm ứng nhiều ngón trong phiên này.
- Nhật ký AI: Codex sinh MapTransform, SeatSelectionViewModel, MapTransformTest; chỉnh SeatMapView/MainActivity/layout/strings. Phát hiện phần tóm tắt đổi chiều cao khiến Canvas nhảy góc nhìn; dành sẵn ba dòng để ổn định vùng vẽ. Sinh viên cần review/hiểu mã và kiểm tra pinch trên thiết bị thật trước khi nộp.
- Mốc tiếp theo: lọc loại ghế và Bottom Sheet đồng bộ hai chiều. Tóm tắt hiện tại chưa phải Expandable Bottom Sheet.


## Mốc bộ lọc, Bottom Sheet và cách thu phóng

- Chạm đúp phóng to tại vị trí chạm, đến mức tối đa thì chạm đúp đưa về toàn cảnh. Nút +/− hỗ trợ, pinch hai ngón vẫn là thao tác chính theo đề. Tách single tap được xác nhận khỏi double tap để không chọn nhầm ghế.
- Lọc theo tất cả/thường/VIP/ghế đôi/còn trống, kết hợp tìm mã ghế không phân biệt hoa thường. Hiển thị số kết quả, điều kiện hiện tại, số mục chọn ngoài bộ lọc; nút Xóa lọc đưa về đầy đủ. Kết quả rỗng có thông báo riêng.
- Bộ lọc chỉ làm mờ các ghế không phù hợp và chặn tap vào chúng; giữ toàn bộ lựa chọn trong ViewModel/Bottom Sheet. Reset View không thay đổi bộ lọc hay lựa chọn.
- BottomSheetBehavior có thu gọn/mở rộng, có thể kéo hoặc nhấn Chi tiết/Thu gọn. Phần mở rộng chiếm khoảng 55% chiều cao khả dụng, danh sách RecyclerView cuộn được.
- Header luôn hiển thị số mục và tổng giá. Mỗi mục có nút xóa; chạm mục sẽ xóa điều kiện lọc, thu gọn bảng và định vị ghế ở mức zoom 3×. Mục ngoài bộ lọc có nhãn giải thích.
- Lựa chọn, loại lọc, query và trạng thái sheet được lưu khi tái tạo Activity; Canvas/bộ lọc/danh sách cùng dùng SeatSelectionViewModel và SeatFilter.

### Kiểm chứng mốc này

- Build thành công, cài/chạy trên Medium Phone API 36.
- Java check: đủ kết quả 200/120/60/20 theo loại, còn trống chỉ trả ghế available, tìm kiếm trim/case-insensitive và không có kết quả.
- Android instrumentation `SeatGestureTest`: **OK (4 tests)**. Gồm pinch hai con trỏ, double tap không chọn nhầm, giữ A3/VIP/query G sau ActivityScenario.recreate, thao tác row định vị/xóa đồng bộ với Canvas/giá.
- Android Studio chạy connected test trả 0 tests trong phiên này; để tránh coi đó là pass giả, đã cài hai APK build và chạy trực tiếp AndroidJUnitRunner qua adb; kết quả 4 tests được runner xác nhận.
- Review UI: chọn A3 rồi lọc VIP vẫn giữ A3 và báo ngoài bộ lọc; chạm row bỏ lọc và focus đúng A3; xóa row đưa tổng về 0; tìm Z999 hiện thông báo trống. Kiểm tra trước đây về chưa thử pinch được thay thế bằng instrumentation ở mốc này.
- Nhật ký AI: Codex thêm SeatFilter, SelectedSeatAdapter, các layout tái sử dụng, Bottom Sheet và wiring MainActivity; chỉnh GestureDetector/Canvas và kiểm thử. Sửa peek height để header không bị cắt, sửa nút Xóa bị xuống dòng. Sinh viên vẫn cần review và hiểu mã nguồn trước khi nộp.
