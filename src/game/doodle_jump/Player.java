// Player.js - класс игрока
class Player {
    constructor(doodle) {
        this.doodlejump = doodle;
        this.left = null;
        this.right = null;
        this.init_player();
    }
    
    static get LENGTH() { return 96; }
    
    init_player() {
        this.x = 210;
        this.y = 600;
        this.vx = 0;
        this.vy = 0.7;
        this.dir = true; // направление (true = right, false = left)
        this.state = 0; // состояние предмета
        this.move = false;
        this.speed = 600;
        this.item = null;
        this.shoes_time = 0;
        this.jump_y = 0;
    }
    
    set_texture(_left, _right) {
        this.left = _left;
        this.right = _right;
    }
    
    is_flying() { return this.state !== 0; }
    set_move(_move) { this.move = _move; }
    
    monster() { this.vy = 0; } // столкновение с монстром
    
    go_x(dt, width) {
        this.x += this.vx * dt;
        // Телепортация через экран
        if (this.x > width - Player.LENGTH / 2)
            this.x = -Player.LENGTH / 2;
        if (this.x < -Player.LENGTH / 2)
            this.x = width - Player.LENGTH / 2;
    }
    
    go_y(dt, c) {
        if (c) this.y += this.vy * dt;
        else this.y -= this.vy * dt;
    }
    
    go_vx(_dir, dt) {
        this.dir = _dir;
        if (this.dir) {
            // Движение вправо
            if (this.vx <= 0) this.vx += 5 * this.speed * dt;
            this.vx += this.speed * dt;
        } else {
            // Движение влево
            if (this.vx >= 0) this.vx -= 5 * this.speed * dt;
            this.vx -= this.speed * dt;
        }
    }
    
    go_vy(dt, c) {
        if (c) this.vy += Doodlejump.G * dt;
        else this.vy -= Doodlejump.G * dt;
    }
    
    reset() {
        this.vx = 0;
        switch (this.state) {
            case 1:
            case 4:
                this.vy = 1400;
                break;
            case 2:
                this.vy = 6000;
                break;
            case 3:
                this.vy = 13000;
                break;
            default:
                this.vy = 600;
                break;
        }
        this.jump_y = Doodlejump.LEAST;
    }
    
    click() {
        this.jump_y = this.y;
        if (this.jump_y < Doodlejump.LEAST) {
            this.doodlejump.total_up = Doodlejump.LEAST - this.jump_y;
            this.move = true;
        }
    }
    
    set_state(_id) {
        if (_id === 0) {
            if (this.state === 4) {
                this.shoes_time--;
                if (this.shoes_time === 0) {
                    this.state = 0;
                }
            } else {
                this.state = 0;
            }
            this.vy = 0;
            return;
        }
        
        if (_id === 1 && this.state === 4) {
            this.shoes_time--;
            if (this.shoes_time === 0) {
                this.state = 1;
            }
            return;
        }
        
        this.move = true;
        this.state = _id;
        this.shoes_time = (this.state === 4) ? 9 : 0;
    }
    
    get_state() { return this.state; }
    
    print_item() {
        if (this.state === 0 || this.state === 1) {
            this.item = null;
            return 0;
        }
        
        this.item = new Item(this.state);
        
        switch (this.state) {
            case 2: // Шляпа
                if (this.dir) {
                    this.item.set_rect(this.x + 20, this.y - Player.LENGTH, this.x + 75, this.y - Player.LENGTH + 35);
                } else {
                    this.item.set_rect(this.x + 20, this.y - Player.LENGTH, this.x + 75, this.y - Player.LENGTH + 35);
                }
                break;
            case 3: // Ракета
                if (this.dir) {
                    this.item.set_rect(this.x + 5, this.y - Player.LENGTH + 25, this.x + 55, this.y);
                } else {
                    this.item.set_rect(this.x + 38, this.y - Player.LENGTH + 25, this.x + 88, this.y);
                }
                break;
            case 4: // Ботинки
                if (this.dir) {
                    this.item.set_rect(this.x + 20, this.y - 20, this.x + 75, this.y + 19);
                } else {
                    this.item.set_rect(this.x + 18, this.y - 20, this.x + 73, this.y + 19);
                }
                break;
        }
        return this.state;
    }
    
    draw(ctx) {
        this.print_item();
        
        // Отрисовка ракеты (под игроком)
        if (this.state === 3 && this.item) {
            this.item.draw(ctx);
        }
        
        // Отрисовка игрока
        if (this.dir && this.right) {
            // Отрисовка вправо
            ctx.drawImage(this.right, this.x, this.y - Player.LENGTH, Player.LENGTH, Player.LENGTH);
        } else if (this.left) {
            // Отрисовка влево
            ctx.drawImage(this.left, this.x, this.y - Player.LENGTH, Player.LENGTH, Player.LENGTH);
        }
        
        // Отрисовка шляпы или ботинок (над игроком)
        if ((this.state === 2 || this.state === 4) && this.item) {
            this.item.draw(ctx);
        }
    }
    
    // Метод для обновления текстуры из загруженных изображений
    updateTextures(leftImg, rightImg) {
        this.left = leftImg;
        this.right = rightImg;
    }
}
