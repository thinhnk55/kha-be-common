# ✅ Quy trình làm việc với Git
1. Nhận nhiệm vụ (task)

2. Tạo issue trên Git

3. Ghi nhớ mã số (ID) của issue

4. Tạo nhánh (branch) mới từ nhánh develop

5. Chỉ làm việc trên nhánh vừa tạo

6. Luôn ghi ID của issue trong nội dung commit

7. Sau khi hoàn thành, review với leader

8. Tạo Pull Request để merge code

# Hướng dẫn Docker
1. Mở terminal v khởi động docker compose -f docker-compose-for-dev.yaml up để chạy postgres và redis
2. Sau đó chạy main từ class com.defi.Main thì sẽ có thể kết nối vào postgres và redis trên.

# Hướng dẫn Response:
Dùng chung BaseResponse, tuân thủ các mã lỗi và message.  

# Hướng dẫn throw Exceptions trong Service
1. Exception ném ra phải là ResponseStatusException với HttpStatus phù hợp
2. Ví dụ
``` java
@Override
    public CategoryDTO getById(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() ->
                 new ResponseStatusException
                 (HttpStatus.NOT_FOUND, ""));
    }
```
3. Tham khảo danh sách HttpStatus thường dùng để đồng bộ trong toàn bộ dự án. Khi gặp khó khăn thì thảo luận với Leader