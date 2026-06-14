package com.pam.waterio

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils

object UsersTable : Table("users") {
    val id = varchar("id", 50)
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val dailyGoalMl = integer("daily_goal_ml").default(2000)
    override val primaryKey = PrimaryKey(id)
}

object WaterEntriesTable : Table("water_entries") {
    val id = varchar("id", 50)
    val userId = varchar("user_id", 50).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val amountMl = integer("amount_ml")
    val timestamp = long("timestamp")
    override val primaryKey = PrimaryKey(id)
}

fun initDatabase(url: String = "jdbc:h2:./water_db;DB_CLOSE_DELAY=-1;") {
    Database.connect(url, driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(UsersTable, WaterEntriesTable)
    }
}
