# 1. 環境準備

まずは開発環境を準備しましょう。

---

## オプション A: GitHub Codespaces を使う

1. このリポジトリで「Code」→「Codespaces」→「Create codespace」をクリック
2. 3分ほど待つと準備完了
3. すべてのツールが使える状態で起動します

✅ 完了! [次へ進む](./02-create-app.md)

---

## オプション B: ローカル PC を使う

### 必要なツール

- **Visual Studio Code**: [ダウンロード](https://code.visualstudio.com/)
- **Java 21**: [ダウンロード](https://adoptium.net/)
- **Docker Desktop**: [ダウンロード](https://www.docker.com/products/docker-desktop/)
- **Azure CLI**: [ダウンロード](https://learn.microsoft.com/cli/azure/install-azure-cli)

### VS Code の準備

VS Code を開いて、統合ターミナルを使えるようにします:

1. VS Code を起動
2. メニューから **ターミナル** → **新しいターミナル** を選択 (または `` Ctrl+` ``)
3. ターミナルが下部に表示されます

**推奨拡張機能** (オプション):
- Extension Pack for Java
- Docker
- Azure Tools

### 確認

VS Code のターミナルで以下のコマンドを実行して、すべてのツールが正しくインストールされているか確認します:

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

VS Code のターミナルで、後で使う基本情報を設定しておきます:

```bash
# リソースグループ名 (好きな名前に変更してください)
export RESOURCE_GROUP="<your-resource-group-name>"

# リージョン
export LOCATION="japaneast"
```

**PowerShell の場合:**
```powershell
# リソースグループ名 (好きな名前に変更してください)
$env:RESOURCE_GROUP = "<your-resource-group-name>"

# リージョン
$env:LOCATION = "japaneast"
```

> 💡 `<your-resource-group-name>` は、例えば `rg-workshop-yourname` のように、あなた専用の名前に置き換えてください。

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
