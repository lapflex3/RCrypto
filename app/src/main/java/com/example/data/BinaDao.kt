package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BinaDao {
    // Assets
    @Query("SELECT * FROM assets")
    fun getAllAssetsFlow(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE symbol = :symbol LIMIT 1")
    suspend fun getAssetBySymbol(symbol: String): AssetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // MultiSig Proposals
    @Query("SELECT * FROM multisig_proposals ORDER BY createdAt DESC")
    fun getAllProposalsFlow(): Flow<List<MultiSigProposalEntity>>

    @Query("SELECT * FROM multisig_proposals WHERE id = :id LIMIT 1")
    suspend fun getProposalById(id: String): MultiSigProposalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: MultiSigProposalEntity)

    @Update
    suspend fun updateProposal(proposal: MultiSigProposalEntity)

    @Query("DELETE FROM multisig_proposals WHERE id = :id")
    suspend fun deleteProposal(id: String)

    // Trading Bots
    @Query("SELECT * FROM trading_bots")
    fun getAllBotsFlow(): Flow<List<TradingBotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: TradingBotEntity)

    @Update
    suspend fun updateBot(bot: TradingBotEntity)

    @Query("DELETE FROM trading_bots WHERE id = :id")
    suspend fun deleteBot(id: String)

    // Trade Orders
    @Query("SELECT * FROM trade_orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<TradeOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TradeOrderEntity)

    @Update
    suspend fun updateOrder(order: TradeOrderEntity)
}
