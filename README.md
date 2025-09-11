<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    XÂY DỰNG ỨNG DỤNG CHAT ROOM DÙNG UDP MULTICAST
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

---
## 📖 1. Giới thiệu hệ thống
Chat Room là một ứng dụng giao tiếp nhóm được thiết kế để hỗ trợ trao đổi tin nhắn theo thời gian thực giữa nhiều người dùng trong cùng một mạng nội bộ (LAN) thông qua giao thức UDP Multicast. Hệ thống cho phép người dùng gửi và nhận tin nhắn mà không cần một máy chủ trung tâm, tận dụng đặc điểm truyền phát dữ liệu đa điểm của multicast để tối ưu hóa hiệu suất mạng. Ứng dụng cung cấp giao diện thân thiện với người dùng, hỗ trợ lưu trữ lịch sử trò chuyện vào file log, và được xây dựng với mục tiêu đơn giản hóa quy trình giao tiếp nhóm trong các môi trường như văn phòng, lớp học, hoặc các sự kiện nội bộ.
Hệ thống bao gồm hai thành phần chính:

MulticastChatMain: Chịu trách nhiệm xử lý các tác vụ mạng, bao gồm gửi và nhận tin nhắn qua UDP Multicast.
MulticastChatApp: Quản lý giao diện người dùng, hiển thị tin nhắn, và lưu trữ dữ liệu chat.

Với thiết kế này, Chat Room không chỉ mang lại trải nghiệm giao tiếp hiệu quả mà còn dễ dàng mở rộng để tích hợp thêm các tính năng nâng cao trong tương lai.
## 📖 1. Giới thiệu hệ thống
Ứng dụng chat Client-Server sử dụng giao thức TCP cho phép nhiều người dùng giao tiếp thời gian thực qua mạng. Server đóng vai trò trung tâm, quản lý kết nối và chuyển tiếp tin nhắn, trong khi client cung cấp giao diện người dùng để gửi và nhận tin nhắn. Dữ liệu được lưu trữ dưới dạng file văn bản thay vì cơ sở dữ liệu, giúp đơn giản hóa triển khai.

Các chức năng chính: 
1. Kết nối và xác thực cơ bản: Client kết nối đến server qua địa chỉ IP và port (mặc định: 1234). Server hỗ trợ nhiều client đồng thời thông qua đa luồng.
2. Gửi và nhận tin nhắn: Người dùng gửi tin nhắn từ client, server nhận và phát tán (broadcast) đến tất cả client khác, hỗ trợ chat nhóm.
3. Lưu trữ lịch sử chat: Server lưu tin nhắn vào file chat_history.txt với định dạng [Timestamp] - [Tên người dùng]: [Nội dung]. Client mới có thể tải lịch sử từ file.
4. Quản lý người dùng: Server theo dõi danh sách client online, cập nhật khi có kết nối/ngắt kết nối. Client hiển thị danh sách này (tùy chọn).
5. Giao diện người dùng: Sử dụng Java Swing với cửa sổ chat gồm khu vực hiển thị tin nhắn, ô nhập văn bản và nút gửi.
6. Xử lý lỗi: Xử lý các trường hợp như mất kết nối hoặc lỗi ghi file.
Hệ thống sử dụng TCP để đảm bảo truyền tin nhắn đáng tin cậy, không hỗ trợ mã hóa hoặc bảo mật nâng cao trong phiên bản cơ bản.

## 🔧 2. Công nghệ sử dụng
Java: Sử dụng Java làm ngôn ngữ chính nhờ tính đa nền tảng, hỗ trợ tốt các thư viện mạng và giao diện người dùng. Phiên bản JDK (ví dụ: OpenJDK 17) được sử dụng để đảm bảo tương thích với các môi trường phát triển hiện đại.

#### Java Core và Multithreading: 
Sử dụng ExecutorService (thuộc gói java.util.concurrent) để quản lý một pool các luồng (thread) trên server, cho phép xử lý đồng thời nhiều kết nối client mà không cần tạo thủ công từng Thread. Điều này giúp cải thiện hiệu suất và quản lý tài nguyên hiệu quả hơn so với sử dụng Thread trực tiếp. Ví dụ: Executors.newFixedThreadPool() được dùng để giới hạn số luồng tối đa, mỗi luồng xử lý một client.

#### Thư Viện và Framework:
Xây dựng giao diện đồ họa (GUI) cho client sử dụng các thành phần của gói javax.swing.*:

        Java Swing: Được sử dụng để xây dựng giao diện đồ họa (GUI) với các thành phần như JFrame, JTextArea, JTextField, và JButton. Swing cung cấp khả năng tùy chỉnh cao và dễ tích hợp với các ứng dụng Java.
        
        Java Networking API: Sử dụng các lớp như MulticastSocket, DatagramPacket, để triển khai giao thức UDP Multicast, cho phép truyền dữ liệu đa điểm hiệu quả.

Swing cung cấp giao diện thân thiện, dễ tùy chỉnh mà không cần thư viện bên ngoài.

#### Công nghệ mạng:
        UDP Multicast: Sử dụng giao thức UDP Multicast để truyền tin nhắn đến nhiều người nhận trong cùng một nhóm (ví dụ: địa chỉ multicast 224.0.0.69, cổng 3000). Phương pháp này giảm tải băng thông so với gửi riêng lẻ đến từng client.
        
        Socket: Được client sử dụng để kết nối đến server thông qua địa chỉ IP và port.
        
        Threading: Sử dụng luồng (thread) trong Java để xử lý nhận tin nhắn đồng thời với giao diện, đảm bảo ứng dụng không bị treo khi chờ dữ liệu mạng.

#### File I/O:

        File I/O: Sử dụng lớp BufferedWriter và FileWriter để ghi lại lịch sử chat vào file chat_log.txt, hỗ trợ lưu trữ dài hạn và dễ dàng truy xuất.

#### Hỗ trợ:

    java.util.Date hoặc java.time.LocalDateTime: Tạo timestamp cho mỗi tin nhắn để ghi vào file và hiển thị trên giao diện, giúp người dùng theo dõi thời gian gửi.
    ArrayList: Quản lý danh sách các client đang kết nối trên server (lưu trữ PrintWriter hoặc DataOutputStream của từng client) để broadcast tin nhắn. Có thể mở rộng để lưu danh sách tên người dùng và trạng thái online/offline.
Không sử dụng thư viện bên ngoài, đảm bảo ứng dụng nhẹ và dễ triển khai trên mọi môi trường Java.
## 🚀 3. Hình ảnh các chức năng

<p align="center">
  <img src="docs/anhGiaoDien.jpg" alt="Ảnh 1" width="800"/>
</p>

<p align="center">
  <em>Hình 1: Ảnh giao diện chat giữa Client-Server  Hình 2: Ảnh 2 Client chat với Server</em>
</p>

<p align="center">
  <img src="docs/anhClientChatServer.jpg" alt="Ảnh 2" width="300"/>
</p>
<p align="center">
  <em> Hình 2: Ảnh 2 Client chat với Server</em>
</p>

<p align="center">
  <img src="docs/anhServertraloiClientLA.jpg" alt="Ảnh 3" width="500"/>
    <img src="docs/anhServertraloiClientHoa.jpg" alt="Ảnh 4" width="500"/>
</p>
<p align="center">
  <em> Hình 3: Ảnh Server trả lời Client Lanh - Hình 4: Ảnh Server trả lời Client Hoa</em>
</p>

<p align="center">
  <img src="docs/anhLichSuChatLuuTxt.jpg" alt="Ảnh 5" width="500"/>
    <img src="docs/anhServerxoaDL.jpg" alt="Ảnh 6" width="300"/>
</p>
<p align="center">
  <em> Hình 5: Ảnh lịch sử chat được lưu vào file txt - Hình 5: Ảnh Server xóa dữ liệu</em>
</p>

<p align="center">
  <img src="docs/anhServerngatKetNoiClient.jpg" alt="Ảnh 7" width="600"/>
</p>
<p align="center">
  <em> Hình 7: Ảnh Server ngắt kết nối với CLient</em>
</p>

## 📝 4. Các bước cài đặt

Yêu cầu hệ thống:

    Java Development Kit (JDK) phiên bản 8 hoặc cao hơn.
    Môi trường phát triển hoặc terminal để biên dịch và chạy mã.

Các bước cài đặt và chạy

Chuẩn bị mã nguồn:

Tải hoặc sao chép hai file: Server.java và Client.java vào cùng một thư mục (package UngDungChat_TCP).


Biên dịch mã nguồn:

Mở terminal, điều hướng đến thư mục chứa file.

Chạy lệnh:

    textjavac UngDungChat_TCP/*.java

Đảm bảo không có lỗi biên dịch.

Chạy Server:

Trong terminal, chạy:

    textjava UngDungChat_TCP.Server

Server sẽ khởi động và hiển thị giao diện, chờ kết nối từ client.

Chạy Client:

Mở một terminal mới (hoặc nhiều terminal để chạy nhiều client).

Chạy:

    textjava UngDungChat_TCP.Client

Nhập tên client (ví dụ: "Lanh", "Hoa:) khi dialog xuất hiện, client sẽ kết nối đến server.

© 2025 AIoTLab, Faculty of Information Technology, DaiNam University. All rights reserved.

---
