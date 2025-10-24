# アプリケーション モダナイゼーション ワークショップ
## Spring Boot (Java) アプリを Azure Container Apps へ

このワークショップでは、Spring Boot (Java) アプリをコンテナー化し、Azure Container Apps にデプロイする基本を学びます。

## 対象者

- Java の基本的な知識がある方
- コンテナーを使ってみたい方

## このワークショップで得られること

### 🎯 学習内容

1. Spring Boot (Java) アプリの作成
2. Docker コンテナーの作成と実行
3. Azure Container Apps へのデプロイ
4. 自動スケーリングとストレージの設定

### 💡 VM 運用からの進化: コンテナー・PaaS のメリット

従来の VM でアプリを運用する場合と比較して、以下のメリットがあります:

#### ✅ 運用負荷の大幅軽減
- **VM の場合**: OS のパッチ適用、ミドルウェア更新、セキュリティ設定などを手動管理
- **Container Apps**: Azure が自動的にインフラを管理、開発に集中できる

#### ✅ 自動スケーリング
- **VM の場合**: 手動でサーバー追加・削除、ロードバランサー設定が必要
- **Container Apps**: トラフィックに応じて自動的にスケール、ゼロスケールでコスト削減

#### ✅ 環境の一貫性
- **VM の場合**: 開発環境と本番環境で差異が発生しやすい
- **コンテナー**: "どこでも同じように動く" 環境を保証

#### ✅ デプロイの高速化
- **VM の場合**: デプロイに数分～数十分かかる
- **Container Apps**: 数秒～数分で新バージョンをデプロイ

#### ✅ コスト最適化
- **VM の場合**: 24 時間稼働でコスト固定、使わない時間も課金
- **Container Apps**: 従量課金、アクセスがない時は自動停止可能

## 必要なもの

- **Azure アカウント** ([無料作成](https://azure.microsoft.com/ja-jp/free/))
- **GitHub Codespaces** または以下がインストール済みの PC:
  - Java 21
  - Docker Desktop
  - Azure CLI

## ワークショップの流れ

| # | 内容 |
|---|------|
| 1 | [環境準備](./docs/01-setup.md) |
| 2 | [アプリ作成](./docs/02-create-app.md) |
| 3 | [コンテナー化](./docs/03-containerize.md) |
| 4 | [Azure へデプロイ](./docs/04-deploy.md) |
| 5 | [スケーリング](./docs/05-scaling.md) |
| 6 | [ストレージ追加](./docs/06-storage.md) |
| 7 | [複数アプリ連携](./docs/07-multiapp.md) |

## 何を作るか

フロントエンドアプリを作り、Azure にデプロイします。
その後、バックエンド API を追加してマイクロサービス構成に進化させます。

```
フロントエンド作成 → コンテナー化 → Azure へデプロイ → バックエンド追加 → サービス間通信
```

### 段階的な学習の流れ

1. **セクション 2〜6**: シンプルなフロントエンドアプリで基礎を学ぶ
2. **セクション 7**: バックエンド API を追加してサービス間通信 (マイクロサービス化)


## 始め方

1. このリポジトリを開く
2. **GitHub Codespaces** で開く
   - ブラウザだけで開発環境が使えます
3. または、ローカル PC にクローン

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/hatasaki/Modernization-Workshop)

## よくある質問

**Q: 費用はかかりますか?**  
A: Azure 無料アカウントで試せます。終了後はリソースを削除してください。

**Q: プログラミング経験が少ないのですが...**  
A: 大丈夫です! コピー&ペーストで進められます。

**Q: ローカル環境がありません**  
A: GitHub Codespaces を使えば、ブラウザだけで OK です!

## 終了後の片付け

```bash
# Azure リソースを削除
az group delete --name rg-workshop --yes
```

---

## 既存アプリのモダナイゼーション

👉 [既存アプリのコンテナー化ガイド](./docs/legacy-app-guide.md)

既存のモノリシックアプリケーションを Azure Container Apps に移行する手順を解説しています。

---

## さあ、始めましょう! 🚀

👉 [1. 環境準備](./docs/01-setup.md) へ進む

---
## Contributing
This project has adopted the Microsoft Open Source Code of Conduct. For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.