class Bullet {
    constructor(_x, _y, player, _doodle) {
        this.x = _x;
        this.y = _y;
        this.doodle = player;
        this.doodlejump = _doodle;
        
        // Загрузка изображения пули
        this.image = this.doodlejump.textures.ball;
        
        if (!this.doodlejump.dir_shoot || this.doodlejump.monster == null) {
            this.vx = 0;
            this.vy = 700;
        } else if (this.doodlejump.monster.y + Monster.MONSTER_HEIGHT / 2 >= this.y) {
            this.vx = 0;
            this.vy = 700;
        } else {
            const delta_x = this.doodlejump.monster.x + Monster.MONSTER_LENGTH / 2 - this.x;
            const delta_y = this.y - this.doodlejump.monster.y + Monster.MONSTER_HEIGHT / 2;
            const delta = Math.sqrt(delta_x * delta_x + delta_y * delta_y);
            this.vx = 700 * delta_x / delta;
            this.vy = 700 * delta_y / delta;
        }
    }
    
    go(dt) {
        this.x += this.vx * dt;
        this.vy -= Doodlejump.G * dt;
        this.y -= this.vy * dt;
    }
    
    down(dt) {
        this.y += dt * this.doodle.vy;
    }
    
    shoot(monster) {
        if (!monster) return false;
        return (this.x > monster.x && 
                this.x < monster.x + Monster.MONSTER_LENGTH && 
                this.y > monster.y - Monster.MONSTER_HEIGHT && 
                this.y < monster.y);
    }
    
    destroy() {
        return (this.x < 0 || 
                this.x > this.doodlejump.width || 
                this.y > this.doodlejump.height);
    }
    
    draw(ctx) {
        if (this.image) {
            ctx.drawImage(this.image, this.x, this.y, 32, 32);
        } else {
            // Fallback: рисуем круг если нет текстуры
            ctx.fillStyle = 'black';
            ctx.beginPath();
            ctx.arc(this.x + 16, this.y + 16, 8, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}
