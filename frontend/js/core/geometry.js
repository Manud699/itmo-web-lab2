
export function calculateHit(x, y, r) {
    if (x >= 0 && y >= 0) return (x + 2 * y) <= r;               
    if (x <= 0 && y >= 0) return x >= -r && y <= r / 2;           
    if (x <= 0 && y <= 0) return (x * x + y * y) <= (r / 2) ** 2; 
    return false;
}