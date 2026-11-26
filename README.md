# CinemaSeatMap

Ứng dụng Android Java + XML cho đồ án giữa kỳ **Phát triển phần mềm cho thiết bị di động**. Màn hình chọn ghế rạp phim được tự vẽ trên **một Canvas**, sử dụng dữ liệu giả lập, không cần tài khoản, API hoặc cơ sở dữ liệu.

## 1. Phạm vi và dữ liệu

| Khu vực | Bố trí | Đối tượng | Giá giả lập |
|---|---|---:|---:|
| Thường | A–F, 20 ghế/hàng | 120 | 80.000đ |
| VIP | G–I, 20 ghế/hàng | 60 | 120.000đ |
| Ghế đôi | J–K, 10 cặp/hàng | 20 | 200.000đ/cặp |
| **Tổng** | **11 hàng, có lối đi giữa** | **200** | |

Một ghế đôi là **một đối tượng chọn**, gồm hai chỗ; tổng phòng có 220 chỗ ngồi. Màn chiếu, nhãn hàng và lối đi là phần vẽ trang trí, không tính vào 200 đối tượng.

Mỗi `Seat` có ID, loại, giá, trạng thái gốc và hình chữ nhật trong hệ tọa độ world. `MockSeatRepository` tạo dữ liệu cố định, tái lập được. Ghế khóa/đã đặt được phân bố xác định theo chỉ số, không đổi ngẫu nhiên khi xoay màn hình.

- Trống: ba sắc độ hồng đậm/vừa/nhạt cho thường/VIP/ghế đôi.
- Đã đặt: xám tím, không chọn được.
- Bị khóa: tối, có dấu ×, không chọn được.
- Đang chọn: hồng sáng có dấu ✓ và viền; lưu riêng trong ViewModel, không sửa trạng thái gốc.

## 2. Chạy dự án

1. Clone repository và mở thư mục gốc trong Android Studio.
2. Cài Android SDK Platform 37 nếu IDE yêu cầu. Dự án dùng compile/target SDK 37, min SDK 24.
3. Dùng Gradle JVM theo cấu hình dự án (Java toolchain 25); Java source compatibility là 11. Gradle Wrapper 9.6.0 và Android Gradle Plugin 9.4.1 được khai báo trong repository.
4. Đợi Gradle Sync, chọn cấu hình **app**, chọn máy ảo/điện thoại API 24 trở lên và Run.

`local.properties` chứa đường dẫn SDK riêng của máy, được bỏ qua bởi Git. Không sao chép đường dẫn SDK của máy khác.

Thư mục trên máy phát triển có dấu tiếng Việt nên `gradle.properties` có `android.overridePathCheck=true` theo thông báo của Android Gradle Plugin. Build bằng IDE đã được xác minh tại đường dẫn này. Khi clone sang máy khác, ưu tiên đường dẫn ngắn, không dấu để giảm vấn đề công cụ Windows.

## 3. Sử dụng

- **Pan:** kéo một ngón, có giới hạn biên; khi toàn sơ đồ đã vừa khung, các trục không cần dịch chuyển được giữ ở giữa.
- **Pinch-to-zoom:** kéo hai ngón ra để phóng to, chụm lại để thu nhỏ; giới hạn 1×–5× so với góc nhìn fit.
- **Chạm đúp:** phóng to tại điểm chạm; tại mức tối đa, chạm đúp về toàn cảnh. Đây là công cụ hỗ trợ, không thay thế pinch theo đề.
- **Chọn/bỏ chọn:** chạm một ghế trống. Chạm ghế đã đặt/bị khóa có thông báo. Thao tác kéo/hai ngón không tạo lựa chọn nhầm.
- **Lọc:** chọn tất cả/thường/VIP/ghế đôi/còn trống, kết hợp tìm một phần mã ghế. Tìm kiếm không phân biệt hoa/thường, bỏ khoảng trắng đầu/cuối.
- Ghế ngoài bộ lọc được làm mờ và không nhận tap; lựa chọn đã có vẫn được giữ. Phần trạng thái ghi số kết quả, điều kiện đang áp dụng và số mục chọn ngoài bộ lọc. Không có kết quả sẽ hiện thông báo riêng.
- **Xóa lọc:** bỏ cả loại lọc và chuỗi tìm kiếm.
- **Toàn cảnh:** nội suy góc nhìn về mặc định trong 280ms, giữ nguyên bộ lọc và danh sách chọn.
- **Bottom Sheet:** kéo tay nắm hoặc nhấn Chi tiết/Thu gọn. Phần thu gọn có số mục/tổng giá. Khi mở rộng, danh sách cuộn được và chỉ chiếm khoảng 55% chiều cao khả dụng.
- **Định vị:** chạm một mục trong Bottom Sheet để bỏ điều kiện lọc, thu gọn bảng và đưa ghế đó vào góc nhìn zoom 3× bằng chuyển động 650ms; thao tác chạm mới có thể ngắt chuyển động.
- **Xóa mục:** nút Xóa trong danh sách bỏ chọn đúng ghế và cập nhật Canvas/tổng tiền ngay.

## 4. Kiến trúc

| Thành phần | Trách nhiệm |
|---|---|
| `model/Seat` | Dữ liệu ghế bất biến, độc lập lớp vẽ |
| `model/SeatFilter` | Quy tắc lọc/tìm kiếm chung |
| `data/MockSeatRepository` | Sinh 200 đối tượng và bố cục world |
| `ui/SeatSelectionViewModel` | Nguồn lựa chọn, bộ lọc/query, tổng giá, trạng thái sheet |
| `ui/MapTransform` | Fit, zoom, pan, giới hạn và đổi tọa độ; Java thuần |
| `ui/SeatMapView` | Một Canvas vẽ sơ đồ, xử lý cử chỉ/hit-testing |
| `ui/SelectedSeatAdapter` | RecyclerView tái sử dụng hàng trong Bottom Sheet |
| `MainActivity` | Kết nối XML, ViewModel, Canvas và BottomSheetBehavior |

`layout/activity_main.xml` và `layout-land/activity_main.xml` dùng các phần XML chung cho bộ lọc, chú thích, điều khiển và Bottom Sheet. Ở chế độ ngang, thông tin/bộ lọc ở cột trái có thể cuộn.

Toàn bộ ghế, màn chiếu, nhãn phân khu, hàng và lối đi được vẽ trong `SeatMapView.onDraw()`. Không tạo 200 Button/View thay cho ghế. Các View trong RecyclerView chỉ là mục của danh sách lựa chọn.

## 5. Chuyển đổi tọa độ và hit-testing

Gọi `(wx, wy)` là tọa độ world, `(sx, sy)` là tọa độ cục bộ trong `SeatMapView`, `s` là tỷ lệ, `(tx, ty)` là dịch chuyển:

```text
sx = wx * s + tx                 wx = (sx - tx) / s
sy = wy * s + ty                 wy = (sy - ty) / s
```

Trước khi zoom, lấy world-point dưới tâm hai ngón. Sau khi giới hạn tỷ lệ mới, tính dịch chuyển để world-point đó vẫn ở dưới tâm cử chỉ:

```text
wx = (focusX - tx) / oldScale
wy = (focusY - ty) / oldScale
newTx = focusX - wx * newScale
newTy = focusY - wy * newScale
```

Sau đó kẹp pan trong vùng cho phép; tại biên, giới hạn di chuyển được ưu tiên. Trục có nội dung nhỏ hơn vùng nhìn được căn giữa.

Tap được đổi về world trước khi kiểm tra `x <= wx <= x + width` và `y <= wy <= y + height` cho từng ghế phù hợp bộ lọc. Ghế không chồng lấn nên kết quả xác định; 200 đối tượng cho phép duyệt tuyến tính đơn giản. Hình học sử dụng chung với dữ liệu vẽ nên hit-testing vẫn đúng sau pan/zoom và gần biên hình chữ nhật ghế.

`GestureDetector` tách single tap đã xác nhận khỏi double tap. `ScaleGestureDetector` nhận hai ngón; chuyển pointer, drag vượt touch slop và ACTION_CANCEL được xử lý để không chọn nhầm.

## 6. Quản lý trạng thái

- `SeatSelectionViewModel` là nguồn thống nhất cho Canvas, bộ lọc và danh sách.
- Lựa chọn dùng `LinkedHashSet` ID để không trùng và giữ thứ tự người dùng chọn. Một bảng tra ID → Seat hỗ trợ truy xuất danh sách/giá.
- Xoay màn hình giữ ViewModel. ID chọn, loại lọc, query và trạng thái sheet còn được lưu trong Bundle để phục hồi Activity.
- Khôi phục chỉ nhận ID hợp lệ của ghế trống, bỏ ID không tồn tại/không hợp lệ và giữ thứ tự danh sách đã lưu.
- Thay đổi kích thước đặt góc nhìn về fit nhưng giữ dữ liệu chọn/lọc. Reset View chỉ tác động `MapTransform`.
- Lọc không xóa ghế đã chọn. Mục ngoài bộ lọc được ghi nhãn trong sheet; định vị mục chủ động bỏ lọc để người dùng nhìn thấy ghế đó.

## 7. Hiệu năng

- Paint, RectF, Path, Shader và Typeface được tạo/tái sử dụng ngoài `onDraw()`.
- Hình học ghế và tọa độ hạt sáng được tạo một lần; không sinh ngẫu nhiên khi vẽ.
- Chỉ yêu cầu `invalidate()` khi dữ liệu, lựa chọn, lọc hoặc góc nhìn thay đổi; animation Reset View dừng khi có thao tác mới hoặc View tách khỏi cửa sổ.
- Pan/zoom chỉ đổi phép biến đổi, không xây lại dữ liệu ghế hay danh sách.
- RecyclerView tái sử dụng hàng cho danh sách dài. Lấy danh sách chọn/tổng tiền theo ID thay vì duyệt lồng toàn bộ sơ đồ.

Đã xem thao tác trên máy ảo; chưa có số đo FPS hoặc kiểm chứng hiệu năng trên điện thoại thật. Không coi kiểm thử chức năng là bằng chứng benchmark.

## 8. Kiểm thử và bằng chứng

Môi trường đã xác minh: **Medium Phone API 36**, build debug/cài/chạy thành công trong Android Studio, review giao diện dọc/ngang.

`SeatGestureTest` chạy trực tiếp bằng AndroidJUnitRunner trên máy ảo, kết quả **OK (7 tests)**:

1. Pinch bằng hai con trỏ MotionEvent phóng to và không chọn ghế.
2. Double tap phóng to và không chọn nhầm.
3. Giữ lựa chọn, bộ lọc và query sau ActivityScenario.recreate.
4. Chạm mục Bottom Sheet định vị/bỏ lọc; xóa mục đồng bộ lựa chọn và tổng tiền.
5. Danh sách hơn 100 ghế giữ đúng thứ tự/không trùng sau recreate, giữ sheet mở, cuộn tới cuối và xóa được mục cuối.
6. Reset View giữ bộ lọc/query/lựa chọn, chỉ đưa zoom về fit.
7. Chạm sát biên ghế sau zoom và pan chọn đúng A3; drag không chọn nhầm.

`MapTransformTest` kiểm tra tâm zoom, min/max, giới hạn pan, đổi tọa độ gần biên của 200 ghế và reset. Hai phương thức đã được chạy bằng Java assertions độc lập; chưa ghi nhận chạy qua Gradle JUnit runner.

Các kiểm tra Java dữ liệu/lọc đã đạt: đúng 200 ID không trùng, 120/60/20 theo loại, không chồng lấn/vượt biên, bộ lọc còn trống, tìm kiếm trim/case-insensitive và kết quả rỗng. Review UI đã xác nhận chọn A3 → lọc VIP vẫn giữ A3 → định vị bỏ lọc/focus đúng → xóa trả tổng về 0; tìm Z999 có thông báo trống.

Có thể chạy lớp `SeatGestureTest` trong Android Studio. Trong phiên phát triển, Gradle connected test báo 0 tests dù build thành công; vì vậy kết quả 7 tests nêu trên được xác minh bằng AndroidJUnitRunner trực tiếp sau khi cài app và test APK, không dựa vào dòng BUILD SUCCESSFUL của lượt 0 tests.

```text
adb install -r -t <app-debug.apk>
adb install -r -t <app-debug-androidTest.apk>
adb shell am instrument -w -r -e class com.example.cinemaseatmap.SeatGestureTest com.example.cinemaseatmap.test/androidx.test.runner.AndroidJUnitRunner
```

## 9. Nhật ký sử dụng AI Agent

AI Agent: **Codex**. AI được sử dụng để đề xuất hướng giải quyết, giải thích kỹ thuật, hỗ trợ viết/sửa mã và kiểm thử. Sinh viên chịu trách nhiệm tìm hiểu, kiểm tra, điều chỉnh và giải thích phần mã đưa vào bài nộp. Nhật ký phân biệt hỗ trợ đã thực hiện với phần sinh viên cần tự rà soát trước khi nộp.

| Ngày | Nhiệm vụ giao Agent / phần sinh hoặc sửa | Lỗi hoặc hạn chế và cách kiểm tra/điều chỉnh |
|---|---|---|
| 25/11/2026 | Phân tích đề và barem, lập quy trình, chốt rạp phim 200 đối tượng | Đề chức năng ghi 50–250, barem ghi 150–250; chọn 200 để đáp ứng cả hai |
| 25/11/2026 | Tạo project Java + XML trong repo bằng Android Studio | Sửa lỗi đường dẫn non-ASCII theo hướng dẫn AGP; xác minh Sync/build/chạy |
| 25/11/2026 | Sinh Seat, MockSeatRepository, SeatMapView và XML dọc/ngang | Java check số lượng/ID/hình học; bổ sung bố cục ngang vì bản đầu sơ đồ quá nhỏ |
| 25/11/2026 | Chỉnh ghế, nền, ánh sáng, bảng màu theo góp ý/ảnh mẫu | Đổi ba màu sang tông tím–hồng; sau đó phân biệt loại ghế bằng sắc độ; review trên máy ảo |
| 25/11/2026 | Sinh MapTransform, xử lý pan/pinch/tap/reset, ViewModel và tóm tắt | Kiểm tra tọa độ; sửa tóm tắt thay đổi chiều cao làm Canvas nhảy góc nhìn; thử chọn sau zoom/pan |
| 25/11/2026 | Thêm double tap hỗ trợ, SeatFilter, Bottom Sheet, adapter và MainActivity wiring | Sửa header sheet bị cắt và nút Xóa xuống dòng; lọc giữ lựa chọn, định vị/xóa được review UI |
| 25/11/2026 | Viết kiểm thử cử chỉ, trạng thái và danh sách dài | Lượt Gradle 0 tests không được coi là pass; chạy trực tiếp AndroidJUnitRunner, 7 tests đạt |
| 25/11/2026 | Hoàn thiện phục hồi thứ tự chọn và tối ưu tra cứu ID | Bản đầu restore theo thứ tự sơ đồ; sửa giữ thứ tự đã lưu, xác minh danh sách đảo thứ tự hơn 100 ghế sau recreate |
| 25/11/2026 | Cập nhật README theo mã hiện tại | Chưa có video/kiểm tra thiết bị thật; ghi rõ các phần còn lại, không coi đồ án đã nộp xong |
| 26/11/2026 | Theo góp ý video demo, AI sửa SeatMapView.focusSeat từ đổi góc nhìn tức thì sang nội suy 650ms; điều chỉnh kiểm thử đồng bộ Bottom Sheet | Build/cài lại bằng Android Studio; chạy lại AndroidJUnitRunner đạt 7 tests. Video quay thao tác thật, nhập từng ký tự và kiểm tra cả kết quả tìm kiếm có/không có; mức độ mượt cần sinh viên review |

### Cách tiếp nhận đề xuất và áp dụng

Quy trình học và áp dụng: yêu cầu AI đề xuất → đọc giải thích và đối chiếu yêu cầu → tìm hiểu mã liên quan → thử trên ứng dụng → điều chỉnh theo kết quả → đưa vào bản nộp khi đã hiểu.

Các yêu cầu đề xuất, nhận xét thiết kế và kiểm chứng trong bảng trên phản ánh quá trình hỗ trợ đã diễn ra. Bảng vẫn ghi rõ phần AI viết hoặc sửa; không coi toàn bộ mã là do sinh viên tự viết. Việc sinh viên đã tự hiểu từng phần cần được chính sinh viên xác nhận, không suy ra từ kết quả kiểm thử tự động.

### Nội dung sinh viên tự tìm hiểu và xác nhận trước khi nộp

Chỉ đánh dấu sau khi tự đọc, chạy thử và giải thích được nội dung tương ứng. Có thể bổ sung ngày thực hiện, tài liệu đã đọc và điều chỉnh thực tế.

- [ ] Giải thích dữ liệu 200 đối tượng, phân biệt ghế đôi và số chỗ ngồi.
- [ ] Giải thích vì sao toàn bộ sơ đồ dùng một Canvas và dữ liệu tách khỏi lớp vẽ.
- [ ] Tự tính ví dụ chuyển đổi world ↔ screen; giải thích zoom quanh tâm hai ngón và giới hạn pan.
- [ ] Giải thích hit-testing sau pan/zoom và cách tách tap khỏi drag/pinch.
- [ ] Theo dõi luồng chọn ghế → ViewModel → Canvas/Bottom Sheet; xóa và định vị một mục.
- [ ] Giải thích giữ lựa chọn khi lọc, Reset View và xoay màn hình.
- [ ] Tự chạy các tình huống lỗi/biên, đọc kết quả kiểm thử và nhận biết giới hạn bằng chứng.
- [ ] Ghi lại phần tự điều chỉnh sau khi tìm hiểu (nếu có), không ghi các bước chưa thực hiện.

### Bổ sung ở mốc kiểm chứng cuối

AI hỗ trợ đối chiếu đề PDF, chuẩn bị nguồn sạch, kiểm tra Sync/build, cập nhật tài liệu và push qua Android Studio sau khi sinh viên tự đăng nhập. Bản nguồn sạch build thành công; repo Public và clone HTTPS không đăng nhập thành công. Video chưa được quay. Các thao tác này không chứng minh thay cho mức độ hiểu mã của sinh viên.

## 10. Video và hoàn tất nộp bài

**Đường dẫn video: chưa bổ sung.** Chưa ghi hình bản nộp.

Kịch bản video gợi ý, khoảng 2–3 phút:

1. Giới thiệu sơ đồ 200 đối tượng, phân khu, ba loại và các trạng thái.
2. Pan và pinch hai ngón ra/vào, chạm chọn/bỏ chọn sau khi đổi góc nhìn.
3. Lọc VIP/ghế đôi, tìm một mã, tìm Z999 để thấy trạng thái trống, Xóa lọc.
4. Mở Bottom Sheet, chọn nhiều ghế, cuộn danh sách, xóa mục và định vị một ghế.
5. Reset View vẫn giữ bộ lọc/lựa chọn; xoay ngang rồi dọc chứng minh giữ trạng thái.

Checklist còn lại trước khi nộp:

- [x] Có đủ chức năng bắt buộc trong mã nguồn và chạy được trên máy ảo.
- [x] Có kiểm thử cử chỉ/trạng thái/danh sách dài và review dọc/ngang.
- [x] README có dữ liệu, kiến trúc, tọa độ, hit-testing, trạng thái, hiệu năng và nhật ký AI.
- [ ] Thử cảm giác pinch/pan trên điện thoại thật; kiểm tra thêm thiết bị/API nếu có.
- [x] Clone GitHub công khai sang thư mục khác; đối chiếu mã với bản nguồn sạch đã build thành công trong Android Studio.
- [ ] Quay video 1–5 phút, đưa đường dẫn truy cập được vào README.
- [ ] Rà yêu cầu lịch sử Git: đã có đủ số lượng commit, nhưng các commit hiện tập trung ngày 25/11/2026. Đề còn yêu cầu phân bổ trong thời gian thực hiện; không khẳng định đạt hoàn toàn chỉ dựa vào số lượng. Không sửa thời gian hoặc tạo commit rỗng.
- [x] Push mã nguồn và tài liệu lên main; repository Public, truy cập không đăng nhập và clone HTTPS thành công.
- [ ] Bổ sung video và kiểm tra quyền truy cập video khi sinh viên quay xong.


## 11. Đối chiếu đề thi ngày 25/11/2026

Nguồn đối chiếu: đề thi giữa kỳ môn Phát triển phần mềm cho thiết bị di động do giảng viên cung cấp. Video được để lại theo yêu cầu của sinh viên; mục này không khẳng định hồ sơ đã nộp.

| Yêu cầu | Kết quả rà soát / bằng chứng |
|---|---|
| Chọn một đề tài, mock data | Đề tài phòng chiếu, dữ liệu cố định, không cần API/database |
| 150–250 đối tượng theo barem; một Canvas | 200 đối tượng, một SeatMapView; 120 thường + 60 VIP + 20 cặp |
| Phân khu, lối đi, màn chiếu, nhãn | Có trong dữ liệu hình học và Canvas; đã review dọc/ngang |
| Ít nhất ba trạng thái, phản hồi chọn/bỏ chọn | Trống/đã đặt/khóa và lớp trạng thái đang chọn; bỏ chọn không đổi trạng thái gốc |
| Pan một ngón, giới hạn biên | Có touch slop, đổi pointer và clamp; drag không chọn nhầm |
| Pinch hai ngón đúng tâm, min/max | ScaleGestureDetector và MapTransform; tỷ lệ fit–5×; instrumentation pinch đạt |
| Hit-testing sau pan/zoom và gần biên | Đổi tọa độ nghịch trước kiểm bounds; test gần biên sau zoom/pan đạt |
| Legend, lọc/tìm kiếm, xóa lọc và kết quả rỗng | Có chú thích trạng thái, lọc loại/còn trống, tìm ID; làm mờ, giữ lựa chọn, báo kết quả rỗng |
| Reset View mượt, giữ bộ lọc/lựa chọn | Animator 280ms; test reset giữ query/filter/ID chọn đạt |
| Bottom Sheet thu gọn/mở rộng, nội dung cuộn | BottomSheetBehavior + RecyclerView; mở rộng khoảng 55% vùng khả dụng |
| Đồng bộ hai chiều, định vị/xóa từng mục | Test định vị/xóa row đạt; UI đã review lựa chọn ngoài bộ lọc |
| Không trùng/mất mục sau lọc/reset/xoay | ViewModel + Bundle + LinkedHashSet; test recreate danh sách hơn 100 mục và thứ tự đạt |
| Tách dữ liệu/trạng thái/lớp vẽ | Seat/Repository, ViewModel, MapTransform và SeatMapView riêng |
| Tái sử dụng tài nguyên đồ họa | Paint/Path/RectF/Shader/Typeface tạo ngoài onDraw; không dựng 200 View trên sơ đồ |
| Build/chạy được | Đã build/cài/chạy qua Android Studio, 7 instrumentation tests đạt trên API 36 |
| README và nhật ký AI | README có nhật ký AI tại mục 9; đường dẫn video chưa bổ sung |
| 5–7 commit có ý nghĩa, phân bổ trong thời gian thực hiện | Đủ số lượng; các commit hiện tập trung cùng ngày. Chưa thể xác nhận tiêu chí phân bổ thời gian; không sửa ngày hoặc tạo commit rỗng |
| Repository và mã nguồn có thể truy cập để nộp | Đã push main, chuyển Public theo yêu cầu sinh viên; API không đăng nhập và clone HTTPS xác minh truy cập công khai |

### Kiểm tra bản xuất sạch và giới hạn bằng chứng

- Đã xuất cây mã nguồn đã commit bằng git archive sang thư mục mới, không mang theo build/.gradle/.idea; chỉ thêm local.properties cho SDK của máy.
- Git clone cục bộ qua công cụ dòng lệnh bị lỗi helper MSYS; không gọi lượt này là clone thành công.
- Gradle CLI tại bản xuất sạch dừng trước biên dịch vì báo project directory không ghi được trong môi trường thực thi giới hạn quyền. Lượt CLI này không được đánh dấu đạt; sau đó đã xác minh lại bằng Android Studio như kết quả cập nhật dưới đây.
- Các build và 7 tests xác nhận ở trên được chạy bằng Android Studio/AndroidJUnitRunner trên bản dự án chính. Kiểm tra lại trên môi trường khác và điện thoại thật là khuyến nghị kiểm chứng thêm; không phải một chức năng bị thiếu trong đề.
- Trạng thái chưa push/riêng tư ở lượt rà soát trước đã được giải quyết: các commit đã đẩy lên main, repo hiện Public và có thể clone không đăng nhập. Video vẫn do sinh viên thực hiện sau.

### Thông điệp commit

Ba commit phát triển cục bộ ban đầu đã được đổi thông điệp sang tiếng Việt, giữ nguyên cây mã nguồn, tác giả và thời gian. Commit khởi tạo `Initial commit` đã có trên GitHub được giữ nguyên; không viết lại lịch sử đã công bố hoặc force-push. Commit báo cáo này và các commit mới tiếp theo dùng tiếng Việt.

Nhật ký AI bổ sung: Codex rà lại toàn văn đề PDF, đối chiếu mã/kiểm thử và file được Git theo dõi; thử bản xuất sạch, ghi rõ lỗi môi trường thay vì đánh dấu pass; đổi thông điệp commit cục bộ theo yêu cầu của sinh viên. Video không được tạo trong lượt này.


## 12. Xác nhận hoàn tất build sạch và GitHub

- Đã push các commit phát triển tiếng Việt lên `main`; không force-push hoặc thay đổi commit khởi tạo đã công bố.
- Theo yêu cầu cụ thể của sinh viên, repository đã chuyển từ Private sang Public. API GitHub không dùng xác thực trả `private: false`; clone HTTPS không đăng nhập đã thành công.
- Repository: https://github.com/nguyenngocphucmkkt-cyber/DAGKPTPMTBDD
- Tạo một bản nguồn riêng bằng git archive từ commit `9269338`, không mang theo `.idea`, `.gradle`, build hoặc APK cũ; chỉ bổ sung local.properties trỏ SDK máy kiểm tra.
- Mở bản riêng trong Android Studio, Gradle Sync đạt, `:app:assembleDebug` báo **BUILD SUCCESSFUL in 15s**, **33 actionable tasks: 33 executed**. APK mới được tạo, không sử dụng kết quả build cũ của dự án chính.
- Đối chiếu 61 file được Git theo dõi với commit: nội dung khớp sau chuẩn hóa LF/CRLF. Bản clone HTTPS từ GitHub cũng ở đúng commit `9269338`. Commit xác nhận tiếp theo chỉ cập nhật README, không thay đổi mã ứng dụng đã kiểm chứng.
- Không coi lượt Gradle CLI bị giới hạn quyền là pass; bằng chứng build sạch là lượt Android Studio nêu trên. Bộ 7 instrumentation tests đạt ở mốc trước vẫn là bằng chứng kiểm thử chức năng hiện tại.
- Nhật ký AI bổ sung: Codex chuẩn bị nguồn sạch, dùng Android Studio để Sync/build, push qua IDE sau khi sinh viên tự đăng nhập; công khai repo theo lựa chọn rõ ràng của sinh viên và kiểm tra bằng API/clone không xác thực. Sinh viên tự xác nhận Trust Project; không thay đổi ngoại lệ Defender để build.
- Ngoài việc sinh viên quay/chèn đường dẫn video, các mục mã nguồn, build, tài liệu, số lượng commit và truy cập repo đã có bằng chứng xác minh; phân bổ commit theo thời gian vẫn cần đối chiếu quy định giảng viên. Thử trên điện thoại thật vẫn là khuyến nghị bổ sung.

## 13. Hồ sơ nộp theo đề

| Thành phần | Nơi cung cấp | Trạng thái |
|---|---|---|
| GitHub Repository và mã nguồn biên dịch/chạy được | https://github.com/nguyenngocphucmkkt-cyber/DAGKPTPMTBDD | Public; đã kiểm chứng build nguồn sạch |
| Lịch sử Git có ý nghĩa, phân bổ trong thời gian thực hiện | Lịch sử commit của repository | Đủ số lượng; cần lưu ý các commit tập trung cùng ngày |
| README đủ nội dung mục 5.4 | README.md | Có nội dung kỹ thuật; thiếu đường dẫn video |
| Nhật ký AI theo mục 5.5 | Mục 9 của README.md | Có nhiệm vụ/phần mã/hạn chế/kiểm chứng; sinh viên tự xác nhận nội dung đã tìm hiểu |
| Video 01–05 phút | Chèn link tại mục 10 | Chưa thực hiện |

Video cần thể hiện Pan, pinch hai ngón, chọn/bỏ chọn, lọc hoặc tìm kiếm, Bottom Sheet, đồng bộ trạng thái, Reset View và giữ trạng thái khi xoay màn hình. Kiểm tra quyền truy cập cả repo và video trước khi gửi.

Chỉ nộp mã nguồn và tài liệu cần thiết; không đưa local.properties, .idea, .gradle, thư mục build hoặc các tệp sinh tự động vào Git. Đề không nêu APK là thành phần bắt buộc. Đề PDF không quy định cổng nộp hoặc mẫu tên bài; thực hiện theo thông báo nộp của giảng viên khi có.
