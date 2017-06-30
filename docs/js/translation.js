var translate = {
    'ru': {
        'How it work': 'Как это работает',
        'Use cases': 'Области применения',
        'Investing': 'Инвестирование',
        'Team': 'Команда',
        'Contacts': 'Контакты'
    },
    'en': {
        'How it work': 'How it work',
        'Use cases': 'Use cases',
        'Investing': 'Investing',
        'Team': 'Team',
        'Contacts': 'Contacts'
    }
};
var lang = navigator.languages && navigator.languages[0] ||
           navigator.language ||
           navigator.userLanguage;
if (localStorage.lang) {
    lang = localStorage.lang;
}
function tr(code) {
    var trArray = translate['en'];
    if (lang === 'ru') {
        trArray = translate['ru'];
    }
    document.write(trArray[code]);
}