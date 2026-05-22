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
    const target = new Date('October 31, 2026 18:00:00').getTime();
    setInterval(() => {
        const now = new Date().getTime();
        const diff = target - now;
        
        const elDays = document.getElementById('days');
        const elHours = document.getElementById('hours');
        const elMins = document.getElementById('mins');
        
        if(elDays && elHours && elMins) {
            elDays.innerText = Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
            elHours.innerText = Math.max(0, Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
            elMins.innerText = Math.max(0, Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)));
        }
    }, 1000);
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
