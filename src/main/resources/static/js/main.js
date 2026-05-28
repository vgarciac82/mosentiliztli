function abrirInvitacion() {
    const portada = document.getElementById('portada');
    const contenido = document.getElementById('contenido');
    const navbar = document.getElementById('navbar');

    if (portada) {
        portada.style.transform = 'translateY(-100%)';
        portada.style.opacity = '0';
    }

    if (contenido && navbar) {
        contenido.classList.remove('hidden');
        navbar.classList.remove('hidden');

        setTimeout(() => {
            contenido.classList.add('opacity-100');
            navbar.classList.add('opacity-100');
            initCountdown();
            lanzarPetalos();
        }, 300);
    }
}

function initCountdown() {
    // Meses en JS son 0-indexados (9 = Octubre) - Este formato evita bugs en Safari iOS
    const target = new Date(2026, 9, 31, 18, 0, 0).getTime();
    
    updateTime(); // Ejecutar inmediatamente para evitar delay
    setInterval(updateTime, 1000);

    function updateTime() {
        const now = new Date().getTime();
        const diff = target - now;
        
        const elDays = document.getElementById('days');
        const elHours = document.getElementById('hours');
        const elMins = document.getElementById('mins');
        const elSecs = document.getElementById('secs');
        
        if(elDays && elHours && elMins && elSecs) {
            elDays.innerText = Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24))).toString().padStart(2, '0');
            elHours.innerText = Math.max(0, Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))).toString().padStart(2, '0');
            elMins.innerText = Math.max(0, Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))).toString().padStart(2, '0');
            elSecs.innerText = Math.max(0, Math.floor((diff % (1000 * 60)) / 1000)).toString().padStart(2, '0');
        }
    }
}

function lanzarPetalos() {
    const container = document.getElementById('particles-container');
    if (!container) return;
    
    const colors = ['#E5D1D1', '#FDFBF7', '#D7B46A']; // Rosa, Marfil, Dorado

    for (let i = 0; i < 35; i++) {
        const p = document.createElement('div');
        p.style.position = 'absolute';
        p.style.left = Math.random() * 100 + 'vw';
        p.style.top = '-20px';
        p.style.width = Math.random() * 10 + 5 + 'px';
        p.style.height = p.style.width;
        p.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
        p.style.borderRadius = '50% 0 50% 50%';
        p.style.opacity = '0.5';
        container.appendChild(p);

        p.animate([
            { transform: 'translateY(0) rotate(0deg)', opacity: 0.6 },
            { transform: `translateY(100vh) translateX(${Math.random() * 100 - 50}px) rotate(360deg)`, opacity: 0 }
        ], { duration: Math.random() * 3000 + 4000, easing: 'ease-out' });
    }
}
