# js2kintoneの設置方法と使い方

## 事前準備

### Groovyのインストール

もしGroovyがローカルにインストールされていない場合、GVM経由でインストールしておいてください。

[http://gvmtool.net/](http://gvmtool.net/)

```
$ curl -s get.gvmtool.net | bash
```

gvm経由でGroovyをインストールしておきます。

```
$ gvm install groovy
$ groovy -v
Groovy Version: 2.4.3 JVM: 1.8.0_40 Vendor: Oracle Corporation OS: Mac OS X
```

### groovy/kintoneCredential.groovy の用意

利用したいkintoneのフォームに接続するためのAPIトークンなどの情報を groovy/kintoneCredential.groovy というファイル名で作成します。kintome側のAPIトークンのアクセス権には「レコード追加」だけを設定してください。

```
account {
    subDomain = 'yourSubDomain'
    appId = "123123"
    apiToken = 'yourAPIToken'
}
```

### Gulpのインストール

これは必須ではありませんが、フォームのテストなどに利用できますので、あると便利です。

[Node.js](https://nodejs.org/download/)は本家サイトやhomebrew経由でインストールしてください。

```
$ npm -v
2.7.4
$ npm install -g gulp
$ gulp -v
[19:19:51] CLI version 3.9.0
[19:19:51] Local version 3.9.0
```

### 依存JavaScriptライブラリのインストール

もしbowerが入っていなければ、インストールした上でbower.jsonにしたがって依存ライブラリをインストールしてください。

```
$ npm install bower -g
$ bower install
```

### gulpfile.jsについて

gulpfile.js では、以下のタスクを登録しています。

* "browserify" ./js/script.js ファイルを browserify して ./dist/browserify/main.js へ書き出し
* "libcombin" bower で管理している依存ライブラリを bower_components.js という名前の1ファイルに結合して ./dist/browserify/ へ書き出し
* "uglify" browserifyされた2つのファイルを uglify して ./dist/js/js2kintone.min.js として書き出し

この他に、.jsファイルや.htmlファイルが更新されると livereload で自動でリロードされる gulp-connect あたりが地味に便利です。


# 利用する各種ソースの生成

## HTMLテンプレートの生成

kintoneCredential.groovyで指定したAPIトークンを使って、HTMLフォームを自動生成します。ここでは kintoneform.html というファイルにリダイレクトしています。

```
$ cd ~/js2kintone/groovy/
$ groovy mkhtml.groovy > ../kintoneform.html
```

このファイルは、[Bootstrap](http://getbootstrap.com/)のCSSで簡易にマークアップされています。各input項目のid/name属性を変更しない限り、自由に編集可能です。

**制限：現在のところ「文字列（1行）」「文字列（複数行）」「ドロップダウン」のフォーム項目に対応しています。**

## JavaScriptファイルの生成

次に、今作成したHTMLテンプレートをハンドリングするJavaScriptのコードを生成します。ここでは js/script.js というファイルにリダイレクトしています（gulpでbrowserifyする元ファイルになります）。

```
$ groovy mkjavascript.groovy > ../js/script.js
```

## AWS Lambda関数の生成

最後に、Lambdaの関数を生成します。

```
$ groovy mklambdafunc.groovy > lambdafunc.js
```

ここで生成されたlambdafunc.jsを使って、ラムダ関数を作成してください。作成するリージョンはどこでも構いませんが、レイテンシーの関係から現在のところUS West(Oregon)をおすすめします。

作成した関数名と、Function ARN は後で利用しますのでメモしておいてください。


# AWSの設定

### IAM ユーザーの作成

AWSのLambdaをブラウザから呼び出す際に利用するIAMユーザを作成します。適当なユーザ（例えば"js2kintone"）を作成し、accessKeyとsecretAccessKeyをメモします。

インラインポリシーで、以下の様な権限を与えます。ここでの "yourFunctionARN" は、先ほどメモしたLambda関数のARNになります。

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Stmt1434340230000",
            "Effect": "Allow",
            "Action": [
                "lambda:InvokeFunction"
            ],
            "Resource": [
                "yourFunctionARN"
            ]
        }
    ]
}
```


# フォームのテスト

## フォームの埋め込みと設定

作成したHTMLフォームを呼び出すために必要なのは以下の2点です。

* js2kintoneConfigで設定をjs2kintone.min.jsに渡す
* フォームをレンダリングした結果を埋め込むplaceholderをHTML上に記述する

embed.html を参考にしてください。設定項目は以下のとおりです。

* id: accessKeyとsecretAccessKeyをBASE64でエンコードし、%で連結してください
* region: Lambda関数を設置したリージョン
* template: 呼び出すHTMLテンプレートファイル名
* lambdaFunc: 呼び出すAWS Lambda関数名
* successMsg: 送信完了後、表示するメッセージ

```
<script type="text/javascript">
var js2kintoneConfig = {
  id: 'accessKeyBASE64%secretAccessKeyBASE64',
  region: 'us-west-2',
  template: 'kintoneform.html',
  lambdaFunc: 'js2kintone',
  successMsg: 'お問い合わせいただき、ありがとうございました。'
};
</script>
<script src="./dist/js/js2kintone.min.js" type="text/javascript"></script>
```

フォームのレンダリング結果を挿入する部分には、以下のHTMLを記述してください。

```
<div id="js2kintoneRender"></div>
```


## フォームのテスト

js2kintoneのディレクトリに戻って、gulpを起動します（ポート8080番を利用するので、他に利用しているものがないか注意してください）。

ブラウザで [http://localhost:8080/embed.html](http://localhost:8080/embed.html) を開いてください。

```
$ cd ..
$ gulp
[19:54:55] Using gulpfile ~/work/js2kintone/gulpfile.js
[19:54:55] Starting 'server'...
[19:54:55] Finished 'server' after 126 ms
[19:54:55] Starting 'connect'...
[19:54:55] Finished 'connect' after 23 ms
[19:54:55] Starting 'build'...
[19:54:55] Starting 'browserify'...
[19:54:56] Starting 'libcombin'...
[19:54:56] Finished 'libcombin' after 16 ms
[19:54:56] Server started http://localhost:8080
[19:54:56] LiveReload started on port 35729
[19:54:56] Finished 'browserify' after 134 ms
[19:54:56] Starting 'uglify'...
[19:54:56] Finished 'uglify' after 5.63 ms
[19:54:56] Finished 'build' after 143 ms
[19:54:56] Starting 'default'...
[19:54:56] Finished 'default' after 18 ms
[BS] Copy the following snippet into your website, just before the closing </body> tag
<script type='text/javascript' id="__bs_script__">//<![CDATA[
    document.write("<script async src='http://HOST:3000/browser-sync/browser-sync-client.2.7.7.js'><\/script>".replace("HOST", location.hostname));
//]]></script>
[BS] Access URLs:
 -------------------------------------
          UI: http://localhost:3001
 -------------------------------------
 UI External: http://192.168.1.13:3001
 -------------------------------------
[BS] 1 file changed (js2kintone.min.js)
```

この段階で、画面に何も出ないなどの不具合があれば、何かが間違っていると思われますので、ブラウザの「デベロッパーツール」などを開いて動作確認をしてください。

必須項目をすべて入れて、送信した結果が Lambda -> kintone に格納されていることを確認して下さい。


# 本番環境への設置

## JavaScriptの設置

dist/js/js2kintone.min.js ファイルをお好みのディレクトリに設置します。

## テンプレートファイルの設置

kintoneから作成したHTMLテンプレート（例ではkintoneform.html）をお好みのディレクトリに設置します。

## JavaScriptの呼び出し

embed.htmlで記述した内容を、問い合わせフォームを設置したいHTMLにコピーします。

* js2kintone.min.js の読み込み先が正しいパスになっていることを確認して下さい。
* js2kintone.min.jsからkintoneform.htmlを読み込めるように設定項目 template を正しく記述してください。

また、Bootstrapを利用する場合には、そのCSSを読み込むことも忘れないで下さい。

## 動作確認

HTMLと関係するファイルをすべてテスト環境などにアップし、動作を確認して下さい。


