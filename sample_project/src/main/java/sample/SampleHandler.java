package sample;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda ハンドラークラス.
 */
public class SampleHandler implements RequestHandler<Map<String, Object>, Object> {

  /**
   * Lambda Function メイン関数.
   *
   * @param event         APIGatewayイベント情報
   * @param lambdaContext トリガー発火時に渡されたJSONデータ内情報
   * @return 処理結果
   */
  @Override
  public Object handleRequest(Map<String, Object> event, Context lambdaContext) {
    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
    response.setIsBase64Encoded(false);
    response.setStatusCode(200);

    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "text/json");
    response.setHeaders(headers);
    response.setBody("Lambda OK");

    return response;
  }

}
