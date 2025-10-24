# 5. スケーリング設定

アクセスが増えたら自動で増やす設定をします。

## 所要時間: 10分

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

## HTTP スケーリングを設定

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

**これで:**
- 最小 1 個のコンテナー
- 最大 10 個まで増える
- 50 リクエスト/個を超えると新しいコンテナーが起動

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. [Azure Portal](https://portal.azure.com/) で Container App `frontend` を開く
2. 左メニュー「スケールとレプリカ」をクリック
3. 「編集とデプロイ」→「リビジョンの新規作成」
4. 「スケール」タブ:
   - **最小レプリカ数**: `1`
   - **最大レプリカ数**: `10`
5. 「スケール ルールの追加」:
   - **ルール名**: `http-rule`
   - **タイプ**: `HTTP スケーリング`
   - **同時要求数**: `50`
6. 「作成」をクリック

</details>

---

## 0 へのスケールダウン (オプション)

アクセスがない時はコンテナーを 0 にしてコスト削減:

<details>
<summary>📘 <b>方法 A: Azure CLI (コマンド)</b></summary>

```bash
az containerapp update \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --min-replicas 0
```

</details>

<details>
<summary>🌐 <b>方法 B: Azure Portal (ブラウザ)</b></summary>

1. Container App を開く
2. 「スケールとレプリカ」→「編集とデプロイ」
3. 「スケール」タブで **最小レプリカ数** を `0` に変更
4. 「作成」

</details>

**注意:** 次のアクセス時に数秒の起動時間がかかります。

> 💡 **VM では不可能なコスト削減**: VM は常時起動が必要ですが、Container Apps はアクセスがない時間は完全に停止してコストゼロに!

---

## 動作確認

### レプリカ数を確認

```bash
az containerapp replica list \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --output table
```

### 負荷をかけて確認 (オプション)

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

レプリカが増えていれば成功!

---

## 完了!

✅ 自動スケーリングが設定できました!

👉 次は [6. ストレージ追加](./06-storage.md) へ
