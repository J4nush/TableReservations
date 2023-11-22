package com.example.tablereservations.service

import com.example.tablereservations.data.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class TablesService(val db: JdbcTemplate) {
    fun findTables(): List<Table> = db.query("select * from tables") { response, _ ->
        Table(response.getInt("id"), response.getInt("seats"), response.getInt("status_id"))
    }

    fun findAvailableTablesForDate(date: Date): List<Table> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return  db.query(
            """SELECT * FROM tables t WHERE t.id NOT IN 
                (select r.id from reservations r WHERE YEAR(r.date) = ? AND MONTH(r.date) = ? AND DAYOFMONTH(r.date) = ? 
                AND r.status_id IN (SELECT s.id from statuses s where s.`key` like 'RES_NEW' OR s.`key` like 'RES_ACC'))""".trimMargin(),
            arrayOf(year, month, day)
        ) { response, _ ->
            Table(response.getInt("id"), response.getInt("seats"), response.getInt("status_id"))
        }
    }

    fun save(table: Table) {
        db.update(
            "insert into tables (id, seats, status_id) values (?, ?, ?)",
            table.id, table.seats, table.status
        )
    }

    fun updateTableStatus(tableId: Int, statusId: Int) {
        db.update("update tables set status_id = ? where id = ?", statusId, tableId)
    }

    fun closeTable(table_id: Int){
        db.update("update tables set status_id = 1 where id = ?", table_id)
    }

    fun openTable(table_id: Int){
        db.update("update tables set status_id = 2 where id = ?", table_id)
    }
    fun addTable(table_seats: Int, table_status: Int) {
        db.update("insert into tables values (?, ?)", arrayOf(table_seats, table_status))
    }

    fun initTables() {
        db.update("insert into tables values (1, 4), (2, 5)")
    }

}