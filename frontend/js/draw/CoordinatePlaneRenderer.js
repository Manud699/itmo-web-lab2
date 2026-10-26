export class CoordinatePlaneRenderer {
    #canvas;
    #ctx;
    #width;
    #height;
    #scale; 
    #centerX;
    #centerY;  

    constructor(canvasId) {
        this.#canvas = document.getElementById(canvasId);
        this.#ctx = this.#canvas.getContext('2d');
        
        
        const cssSize = 500; 
        this.#canvas.style.width = `${cssSize}px`;
        this.#canvas.style.height = `${cssSize}px`;

        const dpr = window.devicePixelRatio || 1;
        this.#canvas.width = cssSize * dpr;
        this.#canvas.height = cssSize * dpr;
        
        this.#ctx.scale(dpr, dpr);

        this.#width = cssSize;
        this.#height = cssSize;
        this.#scale = 40; 
        this.#centerX = this.#width / 2; 
        this.#centerY = this.#height / 2;
    }

    drawBaseGraph(rValue = null) {
        this.#ctx.clearRect(0, 0, this.#width, this.#height);
        
        if (rValue) {
            this.#drawHitShapes(rValue); 
        }
        
        this.#drawAxes(rValue);
    }

    drawPoint(x, y, isHit) {
        const pixelX = this.#centerX + (x * this.#scale);
        const pixelY = this.#centerY - (y * this.#scale); 

        this.#ctx.beginPath();
        this.#ctx.arc(pixelX, pixelY, 1.5, 0, Math.PI * 2);
        this.#ctx.fillStyle = isHit ? '#00FF00' : '#FF0000'; 
        this.#ctx.fill();
        this.#ctx.closePath();
    }

    #drawHitShapes(r) {
        const scaledR = r * this.#scale;
        const scaledHalfR = (r / 2) * this.#scale;

        this.#ctx.fillStyle = '#3399FF';
        this.#ctx.beginPath();

        this.#ctx.fillRect(this.#centerX - scaledR, this.#centerY - scaledHalfR, scaledR, scaledHalfR);

        this.#ctx.moveTo(this.#centerX, this.#centerY);
        this.#ctx.lineTo(this.#centerX + scaledR, this.#centerY);
        this.#ctx.lineTo(this.#centerX, this.#centerY - scaledHalfR);
        this.#ctx.fill();

        this.#ctx.moveTo(this.#centerX, this.#centerY);
        this.#ctx.arc(this.#centerX, this.#centerY, scaledHalfR, Math.PI, 0.5 * Math.PI, true);
        this.#ctx.fill();
    }

    #drawAxes(r) {
        this.#ctx.strokeStyle = '#c9d1d9';
        this.#ctx.fillStyle = '#c9d1d9';
        this.#ctx.lineWidth = 1;
        
        this.#ctx.beginPath();
        
        this.#ctx.moveTo(0, this.#centerY);
        this.#ctx.lineTo(this.#width, this.#centerY);
    
        this.#ctx.moveTo(this.#centerX, 0);
        this.#ctx.lineTo(this.#centerX, this.#height);

        this.#ctx.moveTo(this.#width - 10, this.#centerY - 5);
        this.#ctx.lineTo(this.#width, this.#centerY);
        this.#ctx.lineTo(this.#width - 10, this.#centerY + 5);

        this.#ctx.moveTo(this.#centerX - 5, 10);
        this.#ctx.lineTo(this.#centerX, 0);
        this.#ctx.lineTo(this.#centerX + 5, 10);
        this.#ctx.stroke();

        this.#ctx.font = "bold 14px sans-serif";
        this.#ctx.fillText("X", this.#width - 15, this.#centerY - 15);
        this.#ctx.fillText("Y", this.#centerX + 15, 15);

        if (r) {
            this.#ctx.font = "12px sans-serif";
            const scaledR = r * this.#scale;
            const scaledHalfR = (r / 2) * this.#scale;
            const tickSize = 4;
            
            this.#ctx.beginPath();

            
            const xMarks = [
                { pos: this.#centerX + scaledR, label: "R" },
                { pos: this.#centerX + scaledHalfR, label: "R/2" },
                { pos: this.#centerX - scaledHalfR, label: "-R/2" },
                { pos: this.#centerX - scaledR, label: "-R" }
            ];

            xMarks.forEach(mark => {
                this.#ctx.moveTo(mark.pos, this.#centerY - tickSize);
                this.#ctx.lineTo(mark.pos, this.#centerY + tickSize);
                this.#ctx.fillText(mark.label, mark.pos - 8, this.#centerY + 20);
            });

            
            const yMarks = [
                { pos: this.#centerY - scaledR, label: "R" },
                { pos: this.#centerY - scaledHalfR, label: "R/2" },
                { pos: this.#centerY + scaledHalfR, label: "-R/2" },
                { pos: this.#centerY + scaledR, label: "-R" }
            ];

            yMarks.forEach(mark => {
            
                this.#ctx.moveTo(this.#centerX - tickSize, mark.pos);
                this.#ctx.lineTo(this.#centerX + tickSize, mark.pos);
                this.#ctx.fillText(mark.label, this.#centerX + 10, mark.pos + 4);
            });

            this.#ctx.stroke();
        }
    }
}