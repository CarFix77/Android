class Item {
    constructor(_id) {
        if (_id === 0) return;
        
        this.id = _id;
        this.rect = { left: 0, top: 0, right: 0, bottom: 0 };
        this.trect = { left: 0, top: 0, right: 0, bottom: 0 };
        
        switch (_id) {
            case 1: // spring
                this.h = 14;
                this.l = 20; // spring_length
                this.trect = { left: 402, top: 96, right: 422, bottom: 110 };
                break;
            case 2: // hat
                this.h = 21;
                this.l = 26; // hat_length
                this.trect = { left: 223, top: 278, right: 249, bottom: 299 };
                break;
            case 3: // rocket
                this.h = 37;
                this.l = 25; // rocket_length
                this.trect = { left: 197, top: 264, right: 222, bottom: 301 };
                break;
            case 4: // shoes
                this.h = 24;
                this.l = 30; // shoes_length
                this.trect = { left: 299, top: 202, right: 329, bottom: 226 };
                break;
        }
    }
    
    static get SPRING_LENGTH() { return 20; }
    static get SHOES_LENGTH() { return 30; }
    static get HAT_LENGTH() { return 26; }
    static get ROCKET_LENGTH() { return 25; }
    
    set_rect(x, y, left) {
        this.rect = {
            left: x + left,
            top: y + 2 - this.h,
            right: x + left + this.l,
            bottom: y + 2
        };
    }
    
    set_rect_coords(left, top, right, bottom) {
        this.rect = { left, top, right, bottom };
    }
    
    set_trect(left, top, right, bottom) {
        this.trect = { left, top, right, bottom };
    }
    
    draw(ctx, texture) {
        if (!texture) {
            // Fallback: рисуем цветные прямоугольники если нет текстуры
            ctx.fillStyle = this.getItemColor();
            ctx.fillRect(this.rect.left, this.rect.top, 
                        this.rect.right - this.rect.left, 
                        this.rect.bottom - this.rect.top);
        } else {
            // Отрисовка из текстуры
            const sourceWidth = this.trect.right - this.trect.left;
            const sourceHeight = this.trect.bottom - this.trect.top;
            const destWidth = this.rect.right - this.rect.left;
            const destHeight = this.rect.bottom - this.rect.top;
            
            ctx.drawImage(texture, 
                this.trect.left, this.trect.top, sourceWidth, sourceHeight,
                this.rect.left, this.rect.top, destWidth, destHeight
            );
        }
    }
    
    getItemColor() {
        switch(this.id) {
            case 1: return 'yellow';   // spring
            case 2: return 'blue';     // hat
            case 3: return 'red';      // rocket
            case 4: return 'green';    // shoes
            default: return 'gray';
        }
    }
}
