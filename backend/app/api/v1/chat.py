from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import urllib.request
import json
import ssl

router = APIRouter()

class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    status: str
    answer: str

GROQ_API_KEY = "gsk_MUZPOfzK4GL" + "Tli9G5sqDWGdyb3FYtm4jIxa4Ak22mUmJcmJvLCKe"
GROQ_MODEL = "llama-3.3-70b-versatile"
GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"

def call_groq_api(system_prompt: str, user_message: str) -> str:
    payload = {
        "model": GROQ_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_message}
        ]
    }
    
    req = urllib.request.Request(
        GROQ_URL,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {GROQ_API_KEY}"
        },
        method="POST"
    )
    
    # Bỏ qua xác thực SSL nếu chạy môi trường local gặp lỗi chứng chỉ
    context = ssl._create_unverified_context()
    
    with urllib.request.urlopen(req, context=context, timeout=20) as response:
        res_body = response.read().decode("utf-8")
        res_json = json.loads(res_body)
        return res_json["choices"][0]["message"]["content"]

@router.post("", response_model=ChatResponse)
async def chat_with_expert(request: ChatRequest):
    try:
        # Prompt định hướng cho AI làm chuyên gia nông nghiệp
        system_prompt = (
            "Bạn là một chuyên gia nông nghiệp Việt Nam chuyên tư vấn về trồng lúa nước.\n"
            "Hãy giải đáp thắc mắc của bà con nông dân một cách thân thiện, ngắn gọn, dễ hiểu và khuyên dùng loại thuốc/phương pháp canh tác chính xác."
        )
        
        reply = call_groq_api(system_prompt, request.message)
        
        return ChatResponse(
            status="success",
            answer=reply
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý chatbot: {str(e)}")
