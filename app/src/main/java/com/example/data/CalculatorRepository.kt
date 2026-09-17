package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculatorRepository(private val dao: CalculationHistoryDao) {
  val allHistory: Flow<List<CalculationHistory>> = dao.getAllHistory()

  suspend fun addCalculation(expression: String, result: String) {
    dao.insert(CalculationHistory(expression = expression, result = result))
  }

  suspend fun deleteCalculation(id: Long) {
    dao.deleteById(id)
  }

  suspend fun clearHistory() {
    dao.clearAll()
  }
}
