// Doodlejump.js - главный класс игры
class Doodlejump {
    constructor() {
        console.log("Game constructor");
        this.canvas = null;
        this.ctx = null;
        this.height = 0;
        this.width = 0;
        
        // Игровые константы
        this.G = 900;
        this.LEAST = 500;
        
        // Игровые объекты
        this.doodle = new Player(this);
        this.monster = null;
        this.boards = [];
        this.bullets = [];
        
        // Состояние игры
        this.gameover = false;
        this.dead = false;
        this.gamestart = false;
        this.statistics = false;
        this.settings = false;
        this.pause = false;
        this.music_on = true;
        this.dir_shoot = true;
        
        // Статистика
        this.score = 0;
        this.total_up = 0;
        this.bullet_dt = 0;
        this.this_jump = 0;
        this.this_time = 0;
        
        this.highest_score = 0;
        this.last_score = 0;
        this.last_jump = 0;
        this.max_jump = 0;
        this.last_time = 0;
        this.max_time = 0;
        this.total_time = 0;
        this.total_play = 0;
        this.total_score = 0;
        this.average_score = 0;
        
        this.msg = "click";
        this.lastTime = 0;
        
        // Загрузка текстур
        this.textures = {};
    }
    
    init(canvas) {
        console.log("Game init");
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.height = canvas.height;
        this.width = canvas.width;
        
        this.set_statistics(0);
        this.another_init();
        
        // Запуск игрового цикла
        this.gameLoop();
    }
    
    another_init() {
        this.monster = null;
        this.boards = [];
        this.bullets = [];
        
        // Создаем начальные платформы
        this.boards.push(new Board(190, 600, this, this.doodle));
        this.another_board(0);
        this.another_board(0);
        this.another_board(0);
        this.another_board(0);
        
        this.doodle.init_player();
        this.bullet_dt = 0;
        this.score = 0;
        this.gameover = false;
        this.gamestart = false;
        this.dead = false;
        this.statistics = false;
        this.settings = false;
        this.pause = false;
        this.this_jump = 0;
        this.this_time = 0;
        this.msg = "click";
    }
    
    async loadTextures() {
        // Загрузка изображений для игры
        try {
            this.textures.left = await this.loadImage('left.png');
            this.textures.right = await this.loadImage('right.png');
            this.textures.photo = await this.loadImage('photo.png');
            this.textures.ball = await this.loadImage('ball.png');
            
            this.doodle.updateTextures(this.textures.left, this.textures.right);
        } catch (error) {
            console.error("Error loading textures:", error);
        }
    }
    
    loadImage(src) {
        return new Promise((resolve, reject) => {
            const img = new Image();
            img.onload = () => resolve(img);
            img.onerror = reject;
            img.src = src;
        });
    }
    
    gameLoop(timestamp) {
        if (!this.lastTime) this.lastTime = timestamp;
        const dt = (timestamp - this.lastTime) / 1000;
        this.lastTime = timestamp;
        
        this.update(dt);
        this.draw();
        
        requestAnimationFrame((ts) => this.gameLoop(ts));
    }
    
    update(dt) {
        if (!this.gamestart) return;
        if (this.gameover) return;
        if (this.pause) return;
        
        this.bullet_dt += dt;
        this.this_time += dt;
        
        // Удаляем платформы за пределами экрана
        if (this.height !== 0) {
            while (this.boards.length > 0 && this.boards[0].get_y() > this.height) {
                this.boards.shift();
                this.another_board(-1);
            }
        }
        
        // Создаем монстра
        if (this.width !== 0 && this.monster === null) {
            if (Math.floor(Math.random() * 1000) === 1) {
                this.monster = new Monster(this, this.doodle);
            }
        }
        
        // Обновляем монстра
        if (this.monster !== null) {
            this.monster.go(dt);
        }
        
        // Обновляем пули
        for (let i = 0; i < this.bullets.length; i++) {
            if (this.monster !== null && this.bullets[i].shoot(this.monster)) {
                this.monster = null;
                this.bullets.splice(i, 1);
                i--;
                this.play("beat.ogg");
            }
        }
        
        // Движение пуль
        for (let i = 0; i < this.bullets.length; i++) {
            this.bullets[i].go(dt);
            if (this.bullets[i].destroy()) {
                this.bullets.splice(i, 1);
                i--;
            }
        }
        
        // Обновляем платформы
        for (let i = 0; i < this.boards.length; i++) {
            const temp = this.boards[i];
            temp.go_ahead(dt);
            if (temp.disappear(dt)) {
                this.boards.splice(i, 1);
                i--;
                this.another_board(-1);
            }
        }
        
        // Движение игрока по X
        if (this.width !== 0) {
            this.doodle.go_x(dt, this.width);
        }
        
        // Логика прыжков и движения
        if (this.msg === "click") {
            this.doodle.reset();
            this.msg = "up";
        } else if (this.msg === "up") {
            this.handleUpState(dt);
        } else if (this.msg === "down") {
            this.handleDownState(dt);
        }
    }
    
    handleUpState(dt) {
        if (this.doodle.move) {
            if (this.doodle.y >= 2 * this.height / 5) {
                this.doodle.go_y(dt, false);
            } else {
                // Движение платформ и объектов вниз
                for (let i = 0; i < this.boards.length; i++) {
                    const temp = this.boards[i];
                    temp.move_y(dt);
                    temp.down();
                }
                
                for (let j = 0; j < this.bullets.length; j++) {
                    this.bullets[j].down(dt);
                }
                
                this.total_up -= dt * this.doodle.vy;
                
                if (this.monster !== null) {
                    this.monster.down(dt);
                    if (this.monster.destroy()) this.monster = null;
                }
            }
            
            this.score += (this.height - this.doodle.y) * dt / 10;
            
            if (!this.doodle.is_flying() && this.total_up < 0) {
                this.doodle.set_move(false);
                this.total_up = 0;
            }
        } else {
            this.doodle.go_y(dt, false);
        }
        
        this.doodle.go_vy(dt, false);
        
        if (!this.doodle.is_flying() && this.monster !== null && this.monster.dead()) {
            this.doodle.monster();
            this.dead = true;
        }
        
        if (this.doodle.vy <= 0) {
            this.doodle.set_state(0);
            this.total_up = 0;
            this.msg = "down";
        }
    }
    
    handleDownState(dt) {
        this.doodle.go_y(dt, true);
        this.doodle.go_vy(dt, true);
        
        if (!this.dead && this.monster !== null && this.monster.dead()) {
            this.doodle.click();
            this.msg = "click";
            this.play("monster.ogg");
            this.monster = null;
            this.this_jump++;
        }
        
        for (let i = 0; i < this.boards.length; i++) {
            const temp = this.boards[i];
            if (!this.dead && temp.is_click()) {
                this.doodle.click();
                this.this_jump++;
                
                if (i === this.boards.length - 1) {
                    this.another_board(-1);
                }
                
                this.doodle.set_state(temp.get_state());
                this.msg = "click";
                
                if (temp.destroy()) {
                    this.boards.splice(i, 1);
                    i--;
                    this.another_board(-1);
                }
            }
        }
        
        if (this.height !== 0 && this.doodle.y > this.height + 97) {
            this.play("dead.ogg");
            this.gameover = true;
            const tmp = Math.floor(this.score);
            this.total_score += tmp;
            this.total_play++;
            this.total_time += this.this_time;
            this.last_score = tmp;
            this.last_time = Math.floor(this.this_time);
            this.last_jump = this.this_jump;
            
            if (tmp > this.highest_score) this.highest_score = tmp;
            if (this.this_time > this.max_time) this.max_time = Math.floor(this.this_time);
            if (this.this_jump > this.max_jump) this.max_jump = this.this_jump;
            
            this.this_jump = 0;
            this.this_time = 0;
            this.set_statistics(1);
        }
    }
    
    draw() {
        if (!this.ctx) return;
        
        // Очистка canvas
        this.ctx.fillStyle = 'rgb(255, 182, 193)';
        this.ctx.fillRect(0, 0, this.width, this.height);
        
        // Сетка фона
        this.ctx.strokeStyle = 'rgb(255, 105, 180)';
        this.ctx.lineWidth = 1;
        
        for (let i = 0; i <= this.height; i += 20) {
            this.ctx.beginPath();
            this.ctx.moveTo(0, i);
            this.ctx.lineTo(this.width, i);
            this.ctx.stroke();
        }
        
        for (let i = 0; i <= this.width; i += 20) {
            this.ctx.beginPath();
            this.ctx.moveTo(i, 0);
            this.ctx.lineTo(i, this.height);
            this.ctx.stroke();
        }
        
        // Отрисовка игрока
        this.doodle.draw(this.ctx);
        
        // Отрисовка монстра
        if (this.monster !== null) {
            this.monster.draw(this.ctx);
        }
        
        // Отрисовка платформ
        for (const board of this.boards) {
            this.drawBoard(board);
            board.draw_item(this.ctx, this.textures.photo);
        }
        
        // Отрисовка пуль
        for (const bullet of this.bullets) {
            bullet.draw(this.ctx);
        }
        
        // Отрисовка счета
        this.ctx.fillStyle = 'red';
        this.ctx.font = '22px Arial';
        this.ctx.fillText(Math.floor(this.score).toString(), 15, 30);
        
        // Отрисовка UI в зависимости от состояния игры
        this.drawUI();
    }
    
    drawBoard(board) {
        // Упрощенная отрисовка платформ (цветами вместо текстур)
        this.ctx.fillStyle = this.getBoardColor(board.get_id());
        this.ctx.fillRect(board.get_x(), board.get_y(), Board.BOARD_LENGTH, 28);
    }
    
    getBoardColor(boardId) {
        const colors = {
            0: 'green',    // Зеленая платформа
            1: 'blue',     // Синяя платформа  
            2: 'brown',    // Коричневая платформа
            3: 'white',    // Белая платформа
            4: 'yellow',   // Желтая платформа (исчезающая)
            5: 'orange',
            6: 'red',
            7: 'purple',
            8: 'cyan',
            9: 'magenta'
        };
        return colors[boardId] || 'gray';
    }
    
    drawUI() {
        this.ctx.fillStyle = 'black';
        this.ctx.font = '48px Arial';
        this.ctx.textAlign = 'center';
        
        if (this.settings) {
            this.drawSettingsUI();
        } else if (this.statistics) {
            this.drawStatisticsUI();
        } else if (!this.gamestart) {
            this.drawMainMenu();
        } else if (this.gameover) {
            this.drawGameOver();
        } else if (this.pause) {
            this.drawPauseMenu();
        }
    }
    
    drawSettingsUI() {
        this.ctx.fillText("MUSIC:", this.width / 2, 200);
        this.ctx.fillText("ON      OFF", this.width / 2, 256);
        this.ctx.fillText("AUTO SHOOTING", this.width / 2, 368);
        this.ctx.fillText("ON      OFF", this.width / 2, 424);
        this.ctx.fillText("BACK", this.width / 2, 536);
        
        if (this.dir_shoot) {
            this.ctx.fillText("*", this.width / 2 - 110, 424);
        } else {
            this.ctx.fillText("*", this.width / 2 + 10, 424);
        }
        
        if (this.music_on) {
            this.ctx.fillText("*", this.width / 2 - 110, 256);
        } else {
            this.ctx.fillText("*", this.width / 2 + 10, 256);
        }
    }
    
    drawStatisticsUI() {
        this.set_statistics(0);
        this.ctx.font = '36px Arial';
        this.ctx.textAlign = 'left';
        
        let y = 200;
        this.ctx.fillText(`Highest score: ${this.highest_score}`, 100, y); y += 40;
        this.ctx.fillText(`Average score: ${this.average_score}`, 100, y); y += 40;
        this.ctx.fillText(`Last score: ${this.last_score}`, 100, y); y += 40;
        this.ctx.fillText(`Last jump: ${this.last_jump}`, 100, y); y += 40;
        this.ctx.fillText(`Max jump: ${this.max_jump}`, 100, y); y += 40;
        this.ctx.fillText(`Last time: ${this.last_time} s`, 100, y); y += 40;
        this.ctx.fillText(`Max time: ${this.max_time} s`, 100, y); y += 40;
        this.ctx.fillText(`Total time: ${this.total_time} s`, 100, y); y += 40;
        this.ctx.fillText(`Total play: ${this.total_play}`, 100, y); y += 40;
        
        this.ctx.font = '48px Arial';
        this.ctx.textAlign = 'center';
        this.ctx.fillText("RESET        BACK", this.width / 2, 600);
    }
    
    drawMainMenu() {
        this.ctx.fillText("START", this.width / 2, 200);
        this.ctx.fillText("SETTINGS", this.width / 2, 350);
        this.ctx.fillText("STATISTICS", this.width / 2, 500);
        this.ctx.fillText("EXIT", this.width / 2, 650);
    }
    
    drawGameOver() {
        this.ctx.fillText("GAME OVER", this.width / 2, 300);
        this.ctx.fillText(Math.floor(this.score).toString(), this.width / 2, 400);
    }
    
    drawPauseMenu() {
        this.ctx.fillText("Pausing......", this.width / 2, 200);
        this.ctx.fillText("SETTINGS", this.width / 2, 350);
        this.ctx.fillText("BACK", this.width / 2, 500);
    }
    
    another_board(l) {
        if (l === -1) l = Math.floor(Math.random() * 5);
        let t = 0, p = 0, id = 0;
        let xx = 0;
        
        while (xx < 130) xx = Math.floor(Math.random() * 210);
        
        const lastBoard = this.boards[this.boards.length - 1];
        const newY = lastBoard.get_y() - xx;
        const newX = Math.random() * (400 - Board.BOARD_LENGTH);
        
        switch (l) {
            case 0:
                t = Math.random();
                if (t <= 0.2) {
                    p = Math.random() * (Board.BOARD_LENGTH - Item.SPRING_LENGTH);
                    id = 1;
                } else if (t <= 0.25) {
                    p = Math.random() * (Board.BOARD_LENGTH - Item.HAT_LENGTH);
                    id = 2;
                } else if (t <= 0.28) {
                    p = Math.random() * (Board.BOARD_LENGTH - Item.ROCKET_LENGTH);
                    id = 3;
                } else if (t <= 0.33) {
                    p = Math.random() * (Board.BOARD_LENGTH - Item.SHOES_LENGTH);
                    id = 4;
                }
                this.boards.push(new Greenboard(newX, newY, id, p, this, this.doodle));
                break;
            case 1:
                this.boards.push(new Blueboard(newX, newY, this, this.doodle));
                break;
            case 2:
                this.boards.push(new Brownboard(newX, newY, this, this.doodle));
                break;
            case 3:
                this.boards.push(new Whiteboard(newX, newY, this, this.doodle));
                break;
            case 4:
                this.boards.push(new Yellowboard(newX, newY, this, this.doodle));
                break;
        }
    }
    
    set_statistics(mode) {
        if (mode === 0) {
            // Загрузка статистики из localStorage
            this.highest_score = parseInt(localStorage.getItem('highest_score') || '0');
            this.total_play = parseInt(localStorage.getItem('total_play') || '0');
            this.total_score = parseInt(localStorage.getItem('total_score') || '0');
            this.total_time = parseInt(localStorage.getItem('total_time') || '0');
            this.last_score = parseInt(localStorage.getItem('last_score') || '0');
            this.last_jump = parseInt(localStorage.getItem('last_jump') || '0');
            this.last_time = parseInt(localStorage.getItem('last_time') || '0');
            this.max_jump = parseInt(localStorage.getItem('max_jump') || '0');
            this.max_time = parseInt(localStorage.getItem('max_time') || '0');
            
            this.average_score = this.total_play === 0 ? 0 : this.total_score / this.total_play;
            this.average_score = Math.round(this.average_score * 1000) / 1000;
        } else if (mode === 1) {
            // Сохранение статистики в localStorage
            localStorage.setItem('highest_score', this.highest_score.toString());
            localStorage.setItem('total_play', this.total_play.toString());
            localStorage.setItem('total_score', this.total_score.toString());
            localStorage.setItem('total_time', Math.floor(this.total_time).toString());
            localStorage.setItem('last_score', this.last_score.toString());
            localStorage.setItem('last_jump', this.last_jump.toString());
            localStorage.setItem('last_time', this.last_time.toString());
            localStorage.setItem('max_jump', this.max_jump.toString());
            localStorage.setItem('max_time', this.max_time.toString());
        } else if (mode === 2) {
            // Сброс статистики
            this.highest_score = 0;
            this.total_play = 0;
            this.total_score = 0;
            this.total_time = 0;
            this.last_score = 0;
            this.last_jump = 0;
            this.last_time = 0;
            this.max_jump = 0;
            this.max_time = 0;
            this.set_statistics(1);
        }
    }
    
    play(sound) {
        // Простая реализация звуков (можно добавить Web Audio API)
        console.log("Play sound:", sound);
    }
    
    onTouch(x, y) {
        this.mmx = x;
        this.mmy = y;
        
        if (this.settings) {
            this.handleSettingsTouch(x, y);
        } else if (this.statistics) {
            this.handleStatisticsTouch(x, y);
        } else if (!this.gamestart) {
            this.handleMainMenuTouch(x, y);
        } else if (this.gameover) {
            this.another_init();
        } else if (this.pause) {
            this.handlePauseTouch(x, y);
        } else if (x > this.width - 140 && x < this.width && y > 13 && y < 70) {
            this.pause = true;
        } else if (this.bullet_dt > 0.2) {
            this.bullets.push(new Bullet(this.doodle.x + 48, this.doodle.y - 48, this.doodle, this));
            this.bullet_dt = 0;
            this.play("shoot.ogg");
        }
    }
    
    handleSettingsTouch(x, y) {
        if (x > this.width / 2 - 50 && x < this.width / 2 + 60 && y > 650 && y < 716) {
            this.settings = false;
        }
        if (x > this.width / 2 + 40 && x < this.width / 2 + 100 && y > 450 && y < 500) {
            this.dir_shoot = false;
        }
        if (x > this.width / 2 - 100 && x < this.width / 2 - 45 && y > 450 && y < 500) {
            this.dir_shoot = true;
        }
        if (x > this.width / 2 - 100 && x < this.width / 2 - 45 && y > 200 && y < 265) {
            this.music_on = true;
        }
        if (x > this.width / 2 + 40 && x < this.width / 2 + 100 && y > 200 && y < 265) {
            this.music_on = false;
        }
    }
    
    handleStatisticsTouch(x, y) {
        if (x > this.width / 2 - 140 && x < this.width / 2 - 45 && y > 550 && y < 605) {
            this.set_statistics(2);
        }
        if (x > this.width / 2 + 75 && x < this.width / 2 + 140 && y > 550 && y < 605) {
            this.statistics = false;
        }
    }
    
    handleMainMenuTouch(x, y) {
        if (x > this.width / 2 - 90 && x < this.width / 2 + 90 && y > 140 && y < 210) {
            this.gamestart = true;
        }
        if (x > this.width / 2 - 120 && x < this.width / 2 + 120 && y > 290 && y < 360) {
            this.settings = true;
        }
        if (x > this.width / 2 - 150 && x < this.width / 2 + 150 && y > 450 && y < 510) {
            this.statistics = true;
        }
        if (x > this.width / 2 - 80 && x < this.width / 2 + 70 && y > 590 && y < 648) {
            // Выход из игры
            window.close();
        }
    }
    
    handlePauseTouch(x, y) {
        if (x > this.width / 2 - 100 && x < this.width / 2 + 140 && y > 280 && y < 350) {
            this.settings = true;
        } else if (x > this.width / 2 - 70 && x < this.width / 2 + 70 && y < 510 && y > 440) {
            this.pause = false;
        }
    }
}

// Статические константы
Doodlejump.G = 900;
Doodlejump.LEAST = 500;
