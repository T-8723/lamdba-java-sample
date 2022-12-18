# Lambdaノウハウ

## Lambda においてランタイムに Java を選択した場合のデプロイ手順

### 背景、目的

業務で Lambda+Java の環境での開発を行いましたが、Java での開発は初めて+Lambda も初めての初心者状態からの試行で色々躓きました。  

AWS の各機能についてはだいたいが公式のリファレンスを置いているのでわかる方には不要かと思いますが、自分のように「Java も Lambda も初心者がとりあえず最速で動かしたい」といった場合に、他の方の参考資料や自分の備忘録として残しておこうと思い作成しています。

最低限動作させるところが目的で、細かい設定などは省略しています。

通常、この手の話はネット上ですぐ出てくるのですが、今回の組み合わせで適当なレベル感のものは見当たらず…  

Python や NodeJS はコンソール上で手軽に試行できるようになっているため、Lambda において使用されるランタイムとしてはネット上では Python や NodeJS が主流で、比較的 Java を使用した例は少なく、ネット上の記事を見ても、Java を使用した解説などは少ないようです。  

最終的には API GateWayと接続するLambda 関数の作成について記載していきます。

### 前提条件

- AWS にログインできる
- Lambda の作成ができる権限がある

### 関数の作成～デプロイまでの流れ

1. 関数の作成
2. ローカル環境構築
   1. Java のインストール
   2. Gradle のインストール
   3. Lambda で実行できる構成の構築
   4. ビルド
3. デプロイ

### 1. 関数の作成

- Lambda、関数の作成ボタンから作成画面を開く
  - ランタイムはJava11を選択
  - 「関数の作成」を押下
- 確認

Lambda、関数の作成ボタンから作成画面を開く  
![01_関数の作成](./img/01_関数の作成_01.png)  

ランタイムはJava11を選択  
![02_関数の作成](./img/01_関数の作成_02.png)  

「関数の作成」を押下  
![03_関数の作成](./img/01_関数の作成_03.png)

確認
![04_関数の作成](./img/01_関数の作成_04.png)

### 2. ローカル環境構築

#### 2-1 Java のインストール

- [OpenJDK_11](https://jdk.java.net/java-se-ri/11)のダウンロード
- インストール
  - 配置・解凍
  - パス設定

[OpenJDK_11](https://jdk.java.net/java-se-ri/11)のダウンロード  
![02_OpenJDK_11のダウンロード_01](./img/02_OpenJDK_11のダウンロード_01.png)  

#### 2-2 Gradle のインストール

- [Gradle](https://gradle.org/install/)のダウンロード
- インストール
  - 配置・解凍
  - パス設定

[Gradle](https://gradle.org/install/)のダウンロード
![03_OpenJDK_11のダウンロード_01](./img/03_Gradleのダウンロード_01.png)  
![03_OpenJDK_11のダウンロード_02](./img/03_Gradleのダウンロード_02.png)  

#### 2-3 Lambda で実行できる構成の構築

- Handler設定
  - Lambda としてコードを起動させるためには、HandlerクラスにhandleRequest関数を実装する必要があります。

  ```Java
  /**

  - Lambda ハンドラークラス.
  */
  public class SampleHandler implements RequestHandler<Map<String, Object>, Object> {

    /**

  - Lambda Function メイン関数.
  *
  - @param event         APIGatewayイベント情報
  - @param lambdaContext トリガー発火時に渡されたJSONデータ内情報
  - @return 処理結果
    */
    @Override
    public Object handleRequest(Map<String, Object> event, Context lambdaContext) {
    ..........(処理内容)...........
    }
  }
  ```
  
  - このとき、Lambda 上のハンドラー設定とJavaパッケージの階層を合わせる必要があります。  
    この場合、/sample_project/src/main/javaの下から **/sample/SampleHandler** の **handleRequest** を呼び出すように修正します。  
  - ハンドラー設定
    - 「編集」を押下
    ![05_Handler変更_01](./img/05_Handler変更_01.png)
    - 「ハンドラ」項目の変更
    ![05_Handler変更_02](./img/05_Handler変更_02.png)  
    - **sample.SampleHandler::handleRequest** に変更、保存を押下
    ![05_Handler変更_03](./img/05_Handler変更_03.png)  
    - 確認
    ![05_Handler変更_04](./img/05_Handler変更_04.png)  

- ビルド設定
  - build.gradleファイルにzipファイル圧縮の設定を記載します。

  ```gradle
    task buildZip(type: Zip) {
        from compileJava
        tasks.withType(JavaCompile) {
        options.encoding = 'UTF-8'
        }
        from processResources
        into('lib') {
            from configurations.runtimeClasspath
        }
    }

    .........
    
    build.dependsOn buildZip
  ```

- サンプル
  - サンプルのリポジトリは ../sample_project 以下に配置しています。

#### 2-4 ビルド

- コマンド実行
  - ルートディレクトリで `gradle.build` を実行します。
  ![04_ビルド結果](./img/04_ビルド結果.png)  
- ファイル確認
  - 下記のファイルが生成されている
  - /sample_project/build/distributions/sample_project.zip

### 3. デプロイ

- ビルドしたzipファイルをアップロードします。
  - 「アップロード元」を押下  
  ![06_デプロイ_01](./img/06_デプロイ_01.png)  
  - 「zipまたはjarファイル」を押下  
  ![06_デプロイ_02](./img/06_デプロイ_02.png)
  - 「アップロード」を押下  
  ![06_デプロイ_03](./img/06_デプロイ_03.png)
  - ビルドしたsample_project.zipを選択し、「保存」を押下  
  ![06_デプロイ_04](./img/06_デプロイ_04.png)
  - ビルド後、テストして確認する  
  ![06_デプロイ_05](./img/06_デプロイ_05.png)
  ![06_デプロイ_06](./img/06_デプロイ_06.png)

## API Gatewayとの接続

### API の作成

- API Gatewayから「REST API」を押下  
  ![07_APIの作成_01](./img/07_APIの作成_01.png)  
  ![07_APIの作成_02](./img/07_APIの作成_02.png)  
- APIの作成が初めての場合はダイアログが表示されるため「OK」を押下
  ![07_APIの作成_03](./img/07_APIの作成_03.png)  
- 「新しいAPI」を押下
  ![07_APIの作成_04](./img/07_APIの作成_04.png)  
- 「API名」を入力し「APIの作成」を押下
  ![07_APIの作成_05](./img/07_APIの作成_05.png)  
- 確認
  ![07_APIの作成_06](./img/07_APIの作成_06.png)  

### リソース の作成

- アクションから「リソースの作成」を押下  
  ![08_リソースの作成_01](./img/08_リソースの作成_01.png)
- 「リソース名」を入力し「リソースの作成」を押下  
  ![08_リソースの作成_02](./img/08_リソースの作成_02.png)
- 確認  
  ![08_リソースの作成_03](./img/08_リソースの作成_03.png)

作成したいAPIの構成によっては繰り返しリソースを作成します。

### メソッド の作成

- アクションから「メソッドの作成」を押下  
  ![09_メソッドの作成_01](./img/09_メソッドの作成_01.png)
  ![09_メソッドの作成_02](./img/09_メソッドの作成_02.png)
- メソッドの種類を選択します。  
  ![09_メソッドの作成_03](./img/09_メソッドの作成_03.png)
  ![09_メソッドの作成_04](./img/09_メソッドの作成_04.png)
- 「結合タイプ」を「Lambda関数」、「Lambdaプロキシ統合の使用」を有効化します。※  
  ![09_メソッドの作成_05](./img/09_メソッドの作成_05.png)
- 「Lambda関数」で先ほどアップロードしたLambda関数を選択します。
  ![09_メソッドの作成_06](./img/09_メソッドの作成_06.png)
  ![09_メソッドの作成_07](./img/09_メソッドの作成_07.png)
- 権限の付与を許可します。
  ![09_メソッドの作成_08](./img/09_メソッドの作成_08.png)
- 確認
  ![09_メソッドの作成_09](./img/09_メソッドの作成_09.png)

※このとき、選択する Lambda 関数のレスポンス型は`APIGatewayProxyResponseEvent`に適合する型である必要があります。（Sampleを参照）

### ステージの作成（デプロイ）

- アクションから「APIのデプロイ」を押下  
  ![10_APIのデプロイ_01](./img/10_APIのデプロイ_01.png)
  ![10_APIのデプロイ_02](./img/10_APIのデプロイ_02.png)
- 「デプロイするステージ」を選択（ステージ未作成のため「新しいステージ」を選択）  
  ![10_APIのデプロイ_03](./img/10_APIのデプロイ_03.png)
- 「ステージ名」を入力、「デプロイ」を押下
  ![10_APIのデプロイ_04](./img/10_APIのデプロイ_04.png)
- 確認
  ![10_APIのデプロイ_05](./img/10_APIのデプロイ_05.png)
  ![10_APIのデプロイ_06](./img/10_APIのデプロイ_06.png)
  ![10_APIのデプロイ_07](./img/10_APIのデプロイ_07.png)
