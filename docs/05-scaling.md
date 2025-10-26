# 5. スケーリング設定

アクセスが増えたら自動で増やす設定をします。

---

## 💡 VM との比較: なぜ自動スケーリングが重要か

### VM での従来の運用
- 手動でサーバーを追加・削除する必要がある
- ピーク時に合わせてサーバーを用意 → **常に余剰リソースでコスト増**
- 夜間やアクセスが少ない時も同じコストがかかる
- スケールアウトに数分～数十分かかる

### Container Apps の自動スケーリング
- ✅ **完全自動**: トラフィックに応じて数秒で自動増減
- ✅ **コスト最適化**: 使った分だけ課金、0 スケール可能
- ✅ **運用負荷ゼロ**: 手動でのサーバー管理不要
- ✅ **高速対応**: 急なアクセス増加にも即座に対応

---

## 0 へのスケールダウン (デフォルトで構成済み)

アクセスがない時はコンテナーを 0 にしてコスト削減できます
デフォルトで 0 スケールが有効になっています。

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 0
```

**PowerShell の場合:**
```powershell
az containerapp update `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --min-replicas 0
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で Container App `frontend` を開く
2. メニューの「スケーリング」を選択
3. スケールルールの **最小レプリカ数** を `0` に変更
4. 「新しいリビジョンとして保存」をクリック

</details>

> 💡 **VM では不可能なコスト削減**: VM は常時起動が必要ですが、Container Apps はアクセスがない時間は完全に停止してコスト抑えることがにできます</BR>
ただし、ゼロからのスケール時はコンテナー起動待ちが発生します

---

## [Option] HTTP スケーリングを設定

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

リクエストが増えると自動的にコンテナーが増えます。

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 1 \
  --max-replicas 10 \
  --scale-rule-name http-rule \
  --scale-rule-type http \
  --scale-rule-http-concurrency 50
```

**PowerShell の場合:**
```powershell
az containerapp update `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --min-replicas 1 `
  --max-replicas 10 `
  --scale-rule-name http-rule `
  --scale-rule-type http `
  --scale-rule-http-concurrency 50
```

**これで:**
- 最小 1 個のコンテナー
- 最大 10 個まで増える
- 50 リクエスト/個を超えると新しいコンテナーが起動

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で Container App `frontend` を開く
2. メニューの「スケーリング」を選択
3. スケールルールの **最大レプリカ数**と**最小レプリカ数** をそれぞれ  `10` と `1` に変更
4. 「スケール ルール」の `http-scaler`クリック
   - **同時要求**: `50`に変更
5. 「スケールルールの追加」をクリック
6. 「新しいリビジョンとして保存」をクリック

</details>

---

## 動作確認

### レプリカ数を確認
ポータルの Azure Container Apss のメニュー「リビジョンとレプリカ」を選択し、「実行の状態」欄で確認できます。</BR>
コマンドは下記です。

```bash
az containerapp replica list \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --output table
```

**PowerShell の場合:**
```powershell
az containerapp replica list `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --output table
```

###  [Option] 負荷をかけて確認

```bash
# 100 リクエストを送る
for i in {1..100}; do
  curl https://$APP_URL/ &
done

# レプリカ数を確認
az containerapp replica list \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --output table
```

**PowerShell の場合:**
```powershell
# 100 リクエストを送る
1..100 | ForEach-Object {
  Start-Job -ScriptBlock { curl "https://$env:APP_URL/" }
}

# レプリカ数を確認
az containerapp replica list `
  --name frontend `
  --resource-group $env:RESOURCE_GROUP `
  --output table
```

レプリカが増えていれば成功!

---

## 完了!

✅ 自動スケーリングが設定できました!

👉 次は [6. 複数アプリ連携](./06-multiapp.md) へ
