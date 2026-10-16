// =========================
// DESPESAS
// =========================

// Pega as despesas que já estão salvas.
// Se não existir nenhuma, começa com uma lista vazia.
let despesas = JSON.parse(localStorage.getItem("despesas")) || [];


// =========================
// CADASTRAR DESPESA
// =========================

const formularioDespesa = document.querySelector("#formulario-despesa");

if (formularioDespesa) {

    formularioDespesa.addEventListener("submit", function(event) {

        event.preventDefault();

        const valor = document.querySelector("#valor").value;
        const data = document.querySelector("#data").value;
        const categoria = document.querySelector("#categoria").value;
        const descricao = document.querySelector("#descricao").value;


        const novaDespesa = {
            valor: Number(valor),
            data: data,
            categoria: categoria,
            descricao: descricao
        };


        despesas.push(novaDespesa);


        localStorage.setItem(
            "despesas",
            JSON.stringify(despesas)
        );


        alert("Despesa cadastrada com sucesso!");


        window.location.href = "dashboard.html";

    });

}

// =========================
// MOSTRAR DESPESAS NO DASHBOARD
// =========================

const listaDespesas = document.querySelector("#lista-despesas");

if (listaDespesas) {

    const despesasSalvas =
        JSON.parse(localStorage.getItem("despesas")) || [];

    if (despesasSalvas.length === 0) {

        listaDespesas.innerHTML = `
            <p>Nenhuma despesa cadastrada.</p>
        `;

    } else {

        despesasSalvas.forEach(function(despesa) {

            const item = document.createElement("div");

            item.classList.add("despesa-item");

            item.innerHTML = `
                <div>
                    <strong>${despesa.descricao}</strong>
                    <span>${despesa.categoria}</span>
                </div>

                <div>
                    <strong>R$ ${despesa.valor.toFixed(2)}</strong>
                    <span>${despesa.data}</span>
                </div>
            `;

            listaDespesas.appendChild(item);

        });

    }

}

// =========================
// MOSTRAR DESPESAS NO HISTÓRICO
// =========================

const historicoTabela = document.querySelector("#historico-tabela");

if (historicoTabela) {

    const despesasSalvas =
        JSON.parse(localStorage.getItem("despesas")) || [];

    if (despesasSalvas.length === 0) {

        historicoTabela.innerHTML = `
            <p style="padding: 20px;">
                Nenhuma despesa cadastrada.
            </p>
        `;

    } else {

        // Cabeçalho
        historicoTabela.innerHTML = `
            <div class="historico-cabecalho">
                <span>Valor</span>
                <span>Data</span>
                <span>Categoria</span>
                <span>Descrição</span>
            </div>
        `;

        // Despesas
        despesasSalvas.forEach(function(despesa) {

            const item = document.createElement("div");

            item.classList.add("historico-item");

            item.innerHTML = `
                <span>
                    R$ ${despesa.valor.toFixed(2)}
                </span>

                <span>
                    ${despesa.data}
                </span>

                <span>
                    ${despesa.categoria}
                </span>

                <span>
                    ${despesa.descricao}
                </span>
            `;

            historicoTabela.appendChild(item);

        });

    }

}
// =========================
// RECEITAS
// =========================

let receitas = JSON.parse(localStorage.getItem("receitas")) || [];

const formularioReceita = document.querySelector("#formulario-receita");

if (formularioReceita) {

    formularioReceita.addEventListener("submit", function(event) {

        event.preventDefault();

        const receita = document.querySelector("#receita").value;
        const categoria = document.querySelector("#categoria-receita").value;
        const valor = document.querySelector("#valor-receita").value;
        const descricao = document.querySelector("#descricao-receita").value;
        const data = document.querySelector("#data-receita").value;
        const ocorrencia = document.querySelector("#ocorrencia").value;


        const novaReceita = {
            receita: receita,
            categoria: categoria,
            valor: Number(valor),
            descricao: descricao,
            data: data,
            ocorrencia: ocorrencia
        };


        receitas.push(novaReceita);


        localStorage.setItem(
            "receitas",
            JSON.stringify(receitas)
        );


        alert("Receita cadastrada com sucesso!");


        window.location.href = "dashboard.html";

    });

}

// =========================
// MOSTRAR RECEITAS NO DASHBOARD
// =========================

const rendaAtual = document.querySelector("#renda-atual");

if (rendaAtual) {

    const receitasSalvas =
        JSON.parse(localStorage.getItem("receitas")) || [];

    let totalReceitas = 0;

    receitasSalvas.forEach(function(receita) {
        totalReceitas += receita.valor;
    });

    rendaAtual.textContent =
        `R$ ${totalReceitas.toFixed(2)}`;
}

// =========================
// CALCULAR SALDO DISPONÍVEL
// =========================

const saldoDisponivel = document.querySelector("#saldo-disponivel");

if (saldoDisponivel) {

    const receitasSalvas =
        JSON.parse(localStorage.getItem("receitas")) || [];

    const despesasSalvas =
        JSON.parse(localStorage.getItem("despesas")) || [];

    let totalReceitas = 0;
    let totalDespesas = 0;

    receitasSalvas.forEach(function(receita) {
        totalReceitas += receita.valor;
    });

    despesasSalvas.forEach(function(despesa) {
        totalDespesas += despesa.valor;
    });

    const saldo = totalReceitas - totalDespesas;

    saldoDisponivel.textContent =
        `R$ ${saldo.toFixed(2)}`;
}

// =========================
// PERFIL
// =========================

const btnConfirmar = document.querySelector("#btn-confirmar");

if (btnConfirmar) {

    btnConfirmar.addEventListener("click", function(event) {

        event.preventDefault();

        const salario = document.querySelector("#perfil-salario").value;
        const nome = document.querySelector("#perfil-nome").value;
        const email = document.querySelector("#perfil-email").value;
        const endereco = document.querySelector("#perfil-endereco").value;
        const dependentes = document.querySelector("#perfil-dependentes").value;

        const dadosPerfil = {
            salario: Number(salario),
            nome: nome,
            email: email,
            endereco: endereco,
            dependentes: dependentes
        };

        localStorage.setItem(
            "perfil",
            JSON.stringify(dadosPerfil)
        );

        alert("Dados atualizados com sucesso!");
    });
}
// =========================
// CARREGAR CADASTRO NO PERFIL
// =========================

const cadastroSalvo = JSON.parse(localStorage.getItem("cadastro"));

if (cadastroSalvo) {

    const campoNome = document.querySelector("#perfil-nome");
    const campoEmail = document.querySelector("#perfil-email");
    const campoEndereco = document.querySelector("#perfil-endereco");
    const campoDependentes = document.querySelector("#perfil-dependentes");
    const campoSalario = document.querySelector("#perfil-salario");

    if (campoNome) {
        campoNome.value = cadastroSalvo.nome || "";
    }

    if (campoEmail) {
        campoEmail.value = cadastroSalvo.email || "";
    }

    if (campoEndereco) {
        campoEndereco.value = cadastroSalvo.endereco || "";
    }

    if (campoDependentes) {
        campoDependentes.value = cadastroSalvo.dependentes || "";
    }

    if (campoSalario) {
        campoSalario.value = cadastroSalvo.renda || "";
    }
}
// =========================
// CADASTRO
// =========================

const formularioCadastro = document.querySelector("#formulario-cadastro");

if (formularioCadastro) {

    formularioCadastro.addEventListener("submit", function(event) {

        event.preventDefault();

        const nome = document.querySelector("#nome").value;
        const cpf = document.querySelector("#cpf-cadastro").value;
        const email = document.querySelector("#email").value;
        const renda = document.querySelector("#renda").value;
        const endereco = document.querySelector("#endereco").value;
        const dependentes = document.querySelector("#dependentes").value;
        const senha = document.querySelector("#senha-cadastro").value;
        const confirmarSenha = document.querySelector("#confirmar-senha").value;

        if (senha !== confirmarSenha) {
            alert("As senhas não são iguais.");
            return;
        }

        const dadosCadastro = {
            nome: nome,
            cpf: cpf,
            email: email,
            renda: Number(renda),
            endereco: endereco,
            dependentes: dependentes,
            senha: senha
        };

        localStorage.setItem(
            "cadastro",
            JSON.stringify(dadosCadastro)
        );

        alert("Cadastro realizado com sucesso!");

        window.location.href = "dashboard.html";
    });
}

// =========================
// LOGIN
// =========================

const formularioLogin = document.querySelector("#formulario-login");

if (formularioLogin) {

    formularioLogin.addEventListener("submit", function(event) {

        event.preventDefault();

        const cpfDigitado = document.querySelector("#cpf").value;
        const senhaDigitada = document.querySelector("#senha").value;

        const cadastroSalvo =
            JSON.parse(localStorage.getItem("cadastro"));


        // Verifica se existe cadastro
        if (!cadastroSalvo) {

            alert("Nenhuma conta cadastrada. Crie uma conta primeiro.");

            return;
        }


        // Verifica CPF e senha
        if (
            cpfDigitado === cadastroSalvo.cpf &&
            senhaDigitada === cadastroSalvo.senha
        ) {

            alert("Login realizado com sucesso!");

            window.location.href = "dashboard.html";

        } else {

            alert("CPF ou senha incorretos.");

        }

    });

}