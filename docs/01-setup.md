# 1. 環境準備

まずは開発環境を準備しましょう。

## 所要時間: 10分

---

## オプション A: GitHub Codespaces を使う (推奨)

**一番簡単な方法です!**

1. このリポジトリで「Code」→「Codespaces」→「Create codespace」をクリック
2. 3分ほど待つと準備完了
3. すべてのツールが使える状態で起動します

✅ これだけで完了! [次へ進む](./02-create-app.md)

---

## オプション B: ローカル PC を使う

### 必要なツール

- Java 11: [ダウンロード](https://adoptium.net/)
- Docker Desktop: [ダウンロード](https://www.docker.com/products/docker-desktop/)
- Azure CLI: [ダウンロード](https://learn.microsoft.com/cli/azure/install-azure-cli)

### 確認

```bash
# Java のバージョン確認
java -version

# Docker の確認
docker --version

# Azure CLI の確認
az version
```

すべて表示されれば OK です。

---

## Azure へログイン

### 方法 A: Azure CLI (コマンド)

```bash
# Azure にログイン
az login
```

ブラウザが開くので、Azure アカウントでサインインしてください。

### 方法 B: Azure Portal (ブラウザ)

CLI の代わりにポータルを使う場合:

1. [Azure Portal](https://portal.azure.com/) を開く
2. Azure アカウントでサインイン
3. 左メニューの検索バーから各リソースを作成できます

> 💡 **このワークショップでは、コマンドとポータル両方の手順を記載しています!**

---

## サブスクリプションの選択

### Azure CLI の場合

```bash
# サブスクリプション一覧
az account list --output table

# 使用するサブスクリプションを設定
az account set --subscription "あなたのサブスクリプション名"
```

### Azure Portal の場合

1. ポータル右上のアカウントアイコンをクリック
2. 「ディレクトリの切り替え」から使用するサブスクリプションを選択

---

## 環境変数の設定

後で使う名前を設定しておきます:

```bash
# リソースグループ名
export RESOURCE_GROUP="rg-workshop"

# リージョン
export LOCATION="japaneast"

# ACR 名 (グローバルで一意な名前が必要)
export ACR_NAME="acr$(date +%s)"

# Container Apps 環境名
export ACA_ENV="aca-env"
```

**PowerShell の場合:**
```powershell
$env:RESOURCE_GROUP = "rg-workshop"
$env:LOCATION = "japaneast"
$env:ACR_NAME = "acr$((Get-Date).Ticks)"
$env:ACA_ENV = "aca-env"
```

---

## Azure の拡張機能をインストール

```bash
# Container Apps 拡張機能
az extension add --name containerapp --upgrade
```

---

## 準備完了!

✅ 環境の準備ができました!

👉 次は [2. アプリ作成](./02-create-app.md) へ
