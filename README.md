# JavaScript to kintone embed library [js2kintone]

## これはなに？ / What's this?

kintoneで作成したフォームをHTMLファイルに埋め込むことが出来るライブラリです。このライブラリを使うことで、kintoneで作ったフォームをあなたのWebサイトにそのまま設置することが出来ます。

html(js2kintone) -> AWS Lambda -> kintone と問い合わせフォームの情報は流れてきます。

This is embed library for kintone application form. You can deploy kintone form to your website on the fly.

form informations flow : html(js2kintone) -> AWS Lambda -> kintone

## js2kintoneの機能 / features

* kintoneCredentialファイルを元に、HTMLフォームのテンプレートを自動的に生成します
* AWS Lambdaの関数とブラウザから利用するJavaScriptコードを自動生成します


* HTML template create automatic via your  kintoneCredential file.
* automatically generates a function of AWS Lambda and JavaScript code.

## kintoneってなに？ / What is kintone?

kintoneはサイボウズ株式会社が提供する、データベース型のビジネスアプリケーションを、クラウドで作成することが出来る、新しいタイプのアプリ開発プラットフォームです。

詳しくはこちらをご確認ください：https://kintone.cybozu.com/jp/

kintone is a cloud service that can make the business applications on the cloud provided by Cybozu.

see https://www.kintone.com/

## 必要なもの / Requirements

* kintone account
* AWS account

## サポート環境 / Supported environment

* AWS SDK for JavaScript がサポートするブラウザ環境（Internet Explorer 10.0以上など）

* AWS SDK for JavaScript supports all modern web browsers. see detail this URL.

詳細： http://docs.aws.amazon.com/AWSJavaScriptSDK/guide/browser-intro.html

**IE9以下などのレガシーブラウザには未対応です**

## 使い方 / Setup & Usage

[設置方法と使い方](docs/setup-usage-jp.md)



## Author

[Koichiro Nishijima](https://github.com/k-nishijima)

## Contact

[R3 institute](https://www.r3it.com/)

## License

[Apache v2 License](http://www.apache.org/licenses/LICENSE-2.0.html)
