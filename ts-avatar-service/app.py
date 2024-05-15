from flask import Flask, jsonify, request
from flask_cors import CORS, cross_origin
from flask import Flask, request, jsonify
import numpy as np
import urllib
import cv2
import os
import json
import base64
import traceback
from base64toimage import base64_cv2
# from face_detect import check

app = Flask(__name__)

# TODO:
# ~~1. 获取图片~~
#  ~2. 检测图片是否ok
#  ~3. 人脸检测&切割
#  ~4. 返回base64格式的图片
# 5. 前端传文件
# 6. Dockerfile部署

receive_path = r"./received/"

CORS(app)

@app.route('/api/v1/avatar', methods=["POST"])
def hello():
    # receive file
    data = request.get_data().decode('utf-8')
    data = json.loads(data)
    image_b64 = data.get("img")
    if image_b64 is None or len(image_b64) < 1:
        return jsonify({"msg": "need img in request body"}), 400

    try:
        image_decode = base64.b64decode(image_b64)
        nparr = np.fromstring(image_decode, np.uint8)
        image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        _, buffer = cv2.imencode('.jpg', image)
        # 将二进制图像数据转换为 base64 编码的字符串
        image_b64 = base64.b64encode(buffer).decode('utf-8')
        base64_cv2(image_b64)
        result = image_b64
    except Exception as e:
        return jsonify({"msg": "exception:" + str(traceback.format_exc())}), 500

    if type(result) == dict and result.get("msg") is not None:
        return jsonify(result), 400

    return result, 200


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=17001, debug=True)
