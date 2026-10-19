
const money = n => Number(n||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'});
const getTx = () => JSON.parse(localStorage.getItem('financeai-transacoes') || '[]');
const saveTx = data => localStorage.setItem('financeai-transacoes', JSON.stringify(data));
const today = () => new Date().toISOString().slice(0,10);
const sample = [
 {id:1,type:'despesa',title:'Supermercado',category:'Alimentação',date:today(),value:245.90,status:'Concluída'},
 {id:2,type:'receita',title:'Salário',category:'Salário',date:today(),value:3200,status:'Concluída'},
 {id:3,type:'despesa',title:'Internet',category:'Moradia',date:today(),value:99.90,status:'Concluída'},
 {id:4,type:'despesa',title:'Transporte',category:'Transporte',date:today(),value:85.00,status:'Concluída'},
 {id:5,type:'receita',title:'Freelance',category:'Extra',date:today(),value:450,status:'Concluída'}
];
if(!localStorage.getItem('financeai-transacoes')) saveTx(sample);
function txList(){return getTx().sort((a,b)=>b.date.localeCompare(a.date))}
function sums(){let t=txList();return {in:t.filter(x=>x.type==='receita').reduce((a,x)=>a+ +x.value,0),out:t.filter(x=>x.type==='despesa').reduce((a,x)=>a+ +x.value,0)}}
function addTx(type){const form=document.querySelector('#transaction-form');if(!form)return;form.addEventListener('submit',e=>{e.preventDefault();const fd=new FormData(form);let t=txList();t.push({id:Date.now(),type,title:fd.get('descricao')||fd.get('titulo'),category:fd.get('categoria')||'Outros',date:fd.get('data')||today(),value:Number(fd.get('valor')),status:'Concluída'});saveTx(t);location.href='transacoes.html';});}
function txRows(items){return items.map(x=>`<tr><td><strong>${x.title}</strong></td><td>${x.category}</td><td>${x.date}</td><td><span class="tag ${x.type==='receita'?'green':'red'}">${x.type==='receita'?'Receita':'Despesa'}</span></td><td class="${x.type==='receita'?'positive':'negative'}"><strong>${x.type==='receita'?'+':'−'} ${money(x.value)}</strong></td><td><span class="tag green">${x.status||'Concluída'}</span></td></tr>`).join('')}
function init(){let page=document.body.dataset.page;let t=txList(),s=sums();
 document.querySelectorAll('[data-income]').forEach(e=>e.textContent=money(s.in));document.querySelectorAll('[data-expense]').forEach(e=>e.textContent=money(s.out));document.querySelectorAll('[data-balance]').forEach(e=>e.textContent=money(s.in-s.out));
 const table=document.querySelector('#transactions-body');if(table){let filtered=t;if(page==='receitas')filtered=t.filter(x=>x.type==='receita');if(page==='despesas')filtered=t.filter(x=>x.type==='despesa');table.innerHTML=filtered.length?txRows(filtered):'<tr><td colspan="6" class="empty">Nenhuma movimentação encontrada.</td></tr>';}
 const recent=document.querySelector('#recent-transactions');if(recent)recent.innerHTML=t.slice(0,5).map(x=>`<div class="transaction"><div class="tx-icon">${x.type==='receita'?'↗':'↘'}</div><div class="tx-info"><strong>${x.title}</strong><small>${x.category} · ${x.date}</small></div><div class="tx-amount ${x.type==='receita'?'positive':'negative'}">${x.type==='receita'?'+':'−'}${money(x.value)}<small>${x.type==='receita'?'Receita':'Despesa'}</small></div></div>`).join('');
 const form=document.querySelector('#transaction-form');if(form)addTx(form.dataset.type);
 const search=document.querySelector('#search-transactions');if(search)search.addEventListener('input',()=>{let q=search.value.toLowerCase();if(table)table.innerHTML=txRows(t.filter(x=>(x.title+x.category+x.type).toLowerCase().includes(q)));});
 const auth=document.querySelector('#login-form');if(auth)auth.addEventListener('submit',e=>{e.preventDefault();location.href='dashboard.html'});
 const signup=document.querySelector('#signup-form');if(signup)signup.addEventListener('submit',e=>{e.preventDefault();alert('Cadastro demonstrativo concluído! A integração com backend será feita posteriormente.');location.href='login.html'});
 document.querySelectorAll('[data-demo]').forEach(b=>b.addEventListener('click',()=>alert('Esta ação está pronta para integração com o backend.')));
}
document.addEventListener('DOMContentLoaded',init);
