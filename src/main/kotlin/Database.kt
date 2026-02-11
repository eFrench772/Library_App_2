package com.example.com

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import com.password4j.Password

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:h2:mem:library;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(Books)
            seedBooks()
        }
        transaction {
            SchemaUtils.create(Users)
            seedUsers()
        }
    }

    private fun parseCsvLine(line: String): List<String> {                                               
        val parts = line.split(",")                                                                      
        val trimmedParts = mutableListOf<String>()                                                       

        for (part in parts) {                                                                            
            val trimmed = part.trim()
            trimmedParts.add(trimmed)                                                                    
        }                                                                              
        return trimmedParts
    }

    private fun seedBooks() {
        //runs if books table is currently empty.
        if (Books.selectAll().count() > 0L) {
            return
        }

        val csvFile = File("../Library_App_2/library_booklist.csv")
        if (!csvFile.exists()) {
            println("This CSV for populating books tabe was not found.")
            return
        }

        val allLines: List<String> = csvFile.readLines()
        val linesWithoutHeader: MutableList<String> = mutableListOf()                                        
                                
        // Skip the first line (header)
        for (i in 1 until allLines.size) {                                                                   
            linesWithoutHeader.add(allLines[i])
        }                                                                                                    
                                                                                            
        // Process each line
        for (line in linesWithoutHeader) {
            // Skip blank lines
            if (line.isBlank()) {
                continue
            }

            val parts = parseCsvLine(line)

            // Make sure we have enough columns
            if (parts.size >= 5 && parts.size < 7) {
                // Clean up ISBN (remove .0)
                var isbn: String? = parts[2].replace(".0", "")
                if (isbn == "") {
                    isbn = null
                }

                // Get notes if it exists
                var notes: String? = null
                if (parts.size == 6) {
                    notes = parts[5]
                    if (notes == "") {
                        notes = null
                    }
                }

                // Insert into database
                Books.insert {
                    it[title] = parts[0]
                    it[author] = parts[1]
                    it[isbn13] = isbn
                    it[formatCode] = parts[3]
                    it[locationCode] = parts[4]
                    it[Books.notes] = notes
                }
            }
        }

        val bookCount = Books.selectAll().count()
        println("Loaded " + bookCount + " books from CSV")
            }
    }

    private fun seedUsers() {

        // Insert into database
        Users.insert {
            it[username] = "one"
            it[email] = "one"
            it[passwordHash] = Password.hash("one").addRandomSalt(8).withScrypt().result
            it[role] = false
        }
        val userCount = Users.selectAll().count()
        println("Loaded $userCount Users")
    }

