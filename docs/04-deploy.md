# 4. Azure へデプロイ

Azure Container Apps にデプロイします。

## 所要時間: 20分

---

## リソースグループを作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) を開く
2. 検索バーで「リソース グループ」を検索
3. 「+ 作成」をクリック
4. 以下を入力:
   - **サブスクリプション**: 使用するサブスクリプション
   - **リソース グループ**: `rg-workshop`
   - **リージョン**: `Japan East`
5. 「確認および作成」→「作成」

</details>

---

## Azure Container Registry (ACR) を作成

Docker イメージを保存する場所を作ります。

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
# ACR を作成
az acr create \
  --name $ACR_NAME \
  --resource-group $RESOURCE_GROUP \
  --sku Basic \
  --admin-enabled true
```

2分ほどかかります。

### ACR にログイン

```bash
az acr login --name $ACR_NAME
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「コンテナー レジストリ」を検索して選択
3. 「作成」をクリック
4. 以下を入力:
   - **リソース グループ**: `rg-workshop`
   - **レジストリ名**: 一意の名前 (例: `acrworkshop12345`)
   - **場所**: `Japan East`
   - **SKU**: `Basic`
5. 「確認および作成」→「作成」
6. 作成後、ACR を開く
7. 左メニュー「アクセス キー」→「管理者ユーザー」を有効化

</details>

---

## イメージを ACR にプッシュ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### イメージにタグ付け

```bash
# ACR のログインサーバー名を取得
ACR_SERVER=$(az acr show --name $ACR_NAME --query loginServer -o tsv)

# タグ付け (frontend アプリ)
docker tag frontend:v1 $ACR_SERVER/frontend:v1
```

### プッシュ

```bash
docker push $ACR_SERVER/frontend:v1
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal + Docker (ハイブリッド)</b></summary>

1. Azure Portal で ACR を開く
2. 「アクセス キー」から以下をコピー:
   - **ログイン サーバー** (例: `acrworkshop12345.azurecr.io`)
   - **ユーザー名**
   - **パスワード**

3. ローカルターミナルで:

```bash
# ACR にログイン (パスワード入力を求められます)
docker login <ログインサーバー> -u <ユーザー名>

# タグ付け (frontend アプリ)
docker tag frontend:v1 <ログインサーバー>/frontend:v1

# プッシュ
docker push <ログインサーバー>/frontend:v1
```

4. Portal の ACR → 「リポジトリ」で `frontend` が表示されることを確認

</details>

---

## Container Apps Environment を作成

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az containerapp env create \
  --name $ACA_ENV \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION
```

3〜5分かかります。

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「Container Apps」を検索して選択
3. 「作成」をクリック
4. 「Container Apps Environment の作成」セクションで「新規作成」
5. 以下を入力:
   - **環境名**: `aca-env`
   - **リージョン**: `Japan East`
6. そのまま次に進む (アプリは次のステップで作成)

</details>

---

## Container App をデプロイ

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

### ACR の認証情報を取得

```bash
ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
```

### フロントエンドアプリをデプロイ

```bash
az containerapp create \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --environment $ACA_ENV \
  --image $ACR_SERVER/frontend:v1 \
  --target-port 8080 \
  --ingress external \
  --registry-server $ACR_SERVER \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で「リソースの作成」
2. 「Container Apps」を検索して選択
3. 基本設定:
   - **リソース グループ**: `rg-workshop`
   - **コンテナー アプリ名**: `frontend`
   - **リージョン**: `Japan East`
   - **Container Apps Environment**: 先ほど作成した `aca-env` を選択

4. 「コンテナー」タブ:
   - **イメージのソース**: `Azure Container Registry`
   - **レジストリ**: 作成した ACR を選択
   - **イメージ**: `frontend`
   - **イメージ タグ**: `v1`
   - ✅ 「管理者の資格情報を使用する」にチェック

5. 「イングレス」タブ:
   - ✅ 「イングレスを有効にする」にチェック
   - **イングレス トラフィック**: `任意の場所からのトラフィックを受け入れる`
   - **ターゲット ポート**: `8080`

6. 「スケール」タブ:
   - **最小レプリカ数**: `1`
   - **最大レプリカ数**: `3`

7. 「確認および作成」→「作成」

</details>

---

## アプリにアクセス

### URL を取得

```bash
APP_URL=$(az containerapp show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "アプリの URL: https://$APP_URL"
```

### ブラウザで確認

ブラウザで `https://<表示された URL>` を開いてみましょう。

「フロントエンドアプリへようこそ!」と表示されれば成功!

---

## ログを確認

```bash
az containerapp logs show \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --follow
```

---

## 完了!

✅ Azure でフロントエンドアプリが動いています!

> 💡 **セクション 7 でバックエンド API を追加して、本格的なマイクロサービスにします！**

👉 次は [5. スケーリング](./05-scaling.md) へ
