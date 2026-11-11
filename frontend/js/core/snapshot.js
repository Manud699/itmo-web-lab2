
export function deepFreeze(obj) {
    Object.values(obj).forEach(value =>{
        if(value && typeof value == 'object') deepFreeze(value);
    });
    return Object.freeze(obj);
} 

export function takeSnapshot(state) {
    return deepFreeze(structuredClone(state));
} 