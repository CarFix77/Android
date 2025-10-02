class Monster {
    constructor(doodlejump, player) {
        this.rect = { left: 0, top: 0, right: 0, bottom: 0 };
        this.trect = { left: 65, top: 188, right: 103, bottom: 237 };
        this.doodle = player;
        this.doodlejump = doodlejump;
        
        this.x = Math.random() * (doodlejump.width - Monster.MONSTER_LENGTH);
        this.y = -10 - Monster.MONSTER_HEIGHT;
        this.speed = 0;
        
        while (this.speed < 70)
            this.speed = Math.floor(Math.random() * 130);
            
        this.dir = true;
    }
    
    static get MONSTER_LENGTH() { return 80; }
    static get MONSTER_HEIGHT() { return 90; }
    
    get_dir() {
        this.rect = {
            left: Math.floor(this.x),
            top: Math.floor(this.y - 49),
            right: Math.floor(this.x + Monster.MONSTER_LENGTH),
            bottom: Math.floor(this.y)
        };
        return this.dir;
    }
    
    go(dt) {
        if (this.x > 0 && this.x < this.doodlejump.width - Monster.MONSTER_LENGTH) {
            this.x += this.speed * dt;
            return;
        }
        
        this.speed = -this.speed;
        this.dir = (this.speed > 0);
        
        while (this.x > this.doodlejump.width - Monster.MONSTER_LENGTH || this.x < 0)
            this.x += this.speed * dt;
            
        if (!this.dir) {
            this.trect = { left: 106, top: 188, right: 144, bottom: 237 };
        } else {
            this.trect = { left: 65, top: 188, right: 103, bottom: 237 };
        }
    }
    
    down(dt) {
        this.y += dt * this.doodle.vy;
    }
    
    dead() {
        // Проверка столкновения с игроком слева
        if (this.doodle.x + Player.LENGTH >= this.x && 
            this.doodle.x + Player.LENGTH <= this.x + Monster.MONSTER_LENGTH) {
            if ((this.doodle.y - Player.LENGTH >= this.y - Monster.MONSTER_HEIGHT && 
                 this.doodle.y - Player.LENGTH <= this.y) ||
                (this.doodle.y >= this.y - Monster.MONSTER_HEIGHT && 
                 this.doodle.y <= this.y)) {
                return true;
            }
        }
        
        // Проверка столкновения с игроком справа
        if (this.doodle.x >= this.x && 
            this.doodle.x <= this.x + Monster.MONSTER_LENGTH) {
            if ((this.doodle.y - Player.LENGTH >= this.y - Monster.MONSTER_HEIGHT && 
                 this.doodle.y - Player.LENGTH <= this.y) ||
                (this.doodle.y >= this.y - Monster.MONSTER_HEIGHT && 
                 this.doodle.y <= this.y)) {
                return true;
            }
        }
        
        return false;
    }
    
    destroy() {
        return this.y - Monster.MONSTER_HEIGHT - 10 > this.doodlejump.height;
    }
    
    draw(ctx) {
        this.rect = {
            left: this.x,
            top: this.y - Monster.MONSTER_HEIGHT,
            right: this.x + Monster.MONSTER_LENGTH,
            bottom: this.y
        };
        
        const texture = this.doodlejump.textures.photo;
        if (texture) {
            const sourceWidth = this.trect.right - this.trect.left;
            const sourceHeight = this.trect.bottom - this.trect.top;
            const destWidth = this.rect.right - this.rect.left;
            const destHeight = this.rect.bottom - this.rect.top;
            
            ctx.drawImage(texture,
                this.trect.left, this.trect.top, sourceWidth, sourceHeight,
                this.rect.left, this.rect.top, destWidth, destHeight
            );
        } else {
            // Fallback: рисуем простого монстра
            ctx.fillStyle = 'purple';
            ctx.fillRect(this.rect.left, this.rect.top, destWidth, destHeight);
            
            ctx.fillStyle = 'white';
            ctx.fillRect(this.rect.left + 10, this.rect.top + 10, 15, 15);
            ctx.fillRect(this.rect.right - 25, this.rect.top + 10, 15, 15);
        }
    }
}
