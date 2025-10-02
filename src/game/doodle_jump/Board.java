// Board.js - базовый класс для платформ
class Board {
    constructor(_x, _y, doodlejump, player) {
        this.x = _x;
        this.y = _y;
        this.doodle = player;
        this.doodlejump = doodlejump;
    }
    
    static get BOARD_LENGTH() { return 120; }
    
    move_y(dt) {
        this.y += dt * this.doodle.vy;
    }
    
    get_x() { return this.x; }
    get_y() { return this.y; }
    
    is_click() {
        // Проверка столкновения игрока с платформой
        return (this.doodle.y - this.get_y() >= 0 && 
                this.doodle.y - this.get_y() <= 25 && 
                this.doodle.x >= this.x - Player.LENGTH + 10 && 
                this.doodle.x <= this.x + Board.BOARD_LENGTH - 10);
    }
    
    get_state() { return 0; } // Проверка получения предмета
    go_ahead(t) {} // Движение для синих и коричневых платформ
    destroy() { return false; } // Уничтожение белой платформы
    disappear(dt) { return false; } // Исчезновение желтой платформы
    get_id() { return 0; } // ID типа платформы
    has_item() { return false; }
    down() {} // Падение предмета с платформы
    destroy_item(id) {}
    get_left() { return -1; }
    get_item() { return null; }
    draw_item(ctx, texture) {} // Отрисовка предмета на canvas
}

// Зеленая платформа (может содержать предметы)
class Greenboard extends Board {
    constructor(_x, _y, _id, __x, doodlejump, player) {
        super(_x, _y, doodlejump, player);
        this.left = -1;
        this.item = null;
        
        if (_id !== 0) {
            this.item = new Item(_id);
            this.left = __x;
            this.item.set_rect(Math.floor(this.x), Math.floor(this.y), Math.floor(this.left));
        }
    }
    
    has_item() { return this.item !== null; }
    get_item() { return this.item; }
    destroy_item(id) { if (id !== 1) this.item = null; }
    
    get_state() {
        if (this.item === null) return 0;
        
        switch (this.item.id) {
            case Item.SPRING:
                if (this.doodle.x >= this.x + this.get_left() - Player.LENGTH && 
                    this.doodle.x <= this.x + this.get_left() + Item.SPRING_LENGTH)
                    return 1;
                break;
            case Item.HAT:
                if (this.doodle.x >= this.x + this.get_left() - Player.LENGTH &&
                    this.doodle.x <= this.x + this.get_left() + Item.HAT_LENGTH) {
                    this.destroy_item(2);
                    return 2;
                }
                break;
            case Item.ROCKET:
                if (this.doodle.x >= this.x + this.get_left() - Player.LENGTH &&
                    this.doodle.x <= this.x + this.get_left() + Item.ROCKET_LENGTH) {
                    this.destroy_item(3);
                    return 3;
                }
                break;
            case Item.SHOES:
                if (this.doodle.x >= this.x + this.get_left() - Player.LENGTH &&
                    this.doodle.x <= this.x + this.get_left() + Item.SHOES_LENGTH) {
                    this.destroy_item(4);
                    return 4;
                }
                break;
        }
        return 0;
    }
    
    down() {
        if (this.item !== null)
            this.item.set_rect(Math.floor(this.x), Math.floor(this.y), Math.floor(this.left));
    }
    
    get_left() { return this.left; }
    get_id() { return 0; }
    
    draw_item(ctx, texture) {
        if (this.item !== null)
            this.item.draw(ctx, texture);
    }
}

// Синяя движущаяся платформа
class Blueboard extends Board {
    constructor(_x, _y, doodlejump, player) {
        super(_x, _y, doodlejump, player);
        this.speed = 0;
        while (this.speed < 50)
            this.speed = Math.floor(Math.random() * 150);
    }
    
    go_ahead(dt) {
        if (this.x > 0 && this.x < this.doodlejump.width - Board.BOARD_LENGTH) {
            this.x += this.speed * dt;
            return;
        }
        this.speed = -this.speed;
        while (this.x > this.doodlejump.width - Board.BOARD_LENGTH || this.x < 0)
            this.x += this.speed * dt;
    }
    
    get_id() { return 1; }
}

// Коричневая платформа (движется вверх-вниз)
class Brownboard extends Board {
    constructor(_x, _y, doodlejump, player) {
        super(_x, _y, doodlejump, player);
        this.speed = this.up = this.has_up = 0;
        while (this.up < 50)
            this.up = Math.floor(Math.random() * 200);
        while (this.speed < 50)
            this.speed = Math.floor(Math.random() * 150);
    }
    
    get_id() { return 2; }
    get_y() { return this.y + this.has_up; }
    
    go_ahead(dt) {
        if (this.has_up <= this.up && this.has_up >= -this.up) {
            this.has_up += this.speed * dt;
            return;
        }
        this.speed = -this.speed;
        while (this.has_up > this.up || this.has_up <= -this.up)
            this.has_up += this.speed * dt;
    }
}

// Белая платформа (уничтожается при приземлении)
class Whiteboard extends Board {
    constructor(_x, _y, doodlejump, player) {
        super(_x, _y, doodlejump, player);
    }
    
    destroy() { return true; }
    get_id() { return 3; }
}

// Желтая платформа (исчезает со временем)
class Yellowboard extends Board {
    constructor(_x, _y, doodlejump, player) {
        super(_x, _y, doodlejump, player);
        this.decrease = 6;
    }
    
    disappear(dt) {
        if (this.get_y() < 0) return false;
        this.decrease -= dt;
        if (this.decrease >= 0) return false;
        return true;
    }
    
    get_id() {
        if (this.decrease >= 3) return 4;
        if (this.decrease >= 1.5) return 5;
        if (this.decrease >= 0.7) return 6;
        if (this.decrease >= 0.35) return 7;
        if (this.decrease >= 0.175) return 8;
        return 9;
    }
}
