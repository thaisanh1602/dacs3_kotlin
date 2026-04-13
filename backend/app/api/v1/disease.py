from fastapi import APIRouter, File, UploadFile
from fastapi.responses import JSONResponse
import io
import time

router = APIRouter()

@router.post("/detect")
async def detect_disease(file: UploadFile = File(...)):
    # 1. Nhận luồng byte ảnh từ Điện thoại gọi lên
    image_bytes = await file.read()
    
    # [CỐT LÕI AI]: Giả lập quá trình chạy AI ML Model (Tốn 2 giây xử lý ảnh thực)
    time.sleep(2)
    
    # 🤖 NƠI ĐẶT MODEL.PYTORCH / TENSORFLOW SAU NÀY:
    # tensor_image = preprocess(image_bytes)
    # prediction = model(tensor_image)
    # disease_name = label_map[prediction.argmax()]
    
    # 2. Xây dựng Kịch bản AI trả lời Khám Bệnh Xong:
    result = {
        "success": True,
        "disease_name": "Bệnh Đạo Ôn (Rice Blast)",
        "confidence_score": 0.963, 
        "risk_level": "High",
        "description": "Phát hiện nhiều vết nứt hình mắt én trên mặt lá do bào tử nấm Pyricularia oryzae gây ra.",
        "treatment_recommendation": (
            "1. Dừng ngay việc bón Đạm (Urê) và giữ mực nước 3-5cm.\n"
            "2. Phun đặc trị các loại thuốc có gốc Tricyclazole (VD: Beam 75WP) "
            "hoặc Isoprothiolane ngay vào lúc trời mát chiều muộn."
        )
    }
    
    # 3. Trả về Điện thoại ở dạng JSON
    return JSONResponse(content=result)
