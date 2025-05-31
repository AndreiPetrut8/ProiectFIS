
-- Tabel formații
CREATE TABLE formations (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            description TEXT,
                            user_id INT,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- Tabel jucători
CREATE TABLE players (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         first_name VARCHAR(50) NOT NULL,
                         shirt_number INT UNIQUE,
                         position VARCHAR(30) NOT NULL,
                         user_id INT,
                         suggestion VARCHAR(255),
                         coach_user_id INT,
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE statistics (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            minutes_played INT DEFAULT 0,
                            goals INT DEFAULT 0,
                            assists INT DEFAULT 0,
                            yellow_cards INT DEFAULT 0,
                            red_cards INT DEFAULT 0,
                            passes_completed INT DEFAULT 0,
                            shots_on_target INT DEFAULT 0,
                            player_id INT,
                            user_id INT,
                            FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

