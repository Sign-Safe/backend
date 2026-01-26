-- Document
create table if not exists documents (
    id bigint not null auto_increment primary key,
    user_id varchar(128),
    source_type varchar(16) not null,
    filename varchar(512),
    content_type varchar(128),
    storage_path varchar(1024),
    raw_text longtext,
    created_at timestamp not null default current_timestamp
);

create index idx_documents_created_at on documents(created_at);

-- Analysis job
create table if not exists analysis_jobs (
    id bigint not null auto_increment primary key,
    document_id bigint not null,
    status varchar(16) not null,
    model varchar(64),
    started_at timestamp null,
    finished_at timestamp null,
    error_message longtext,
    created_at timestamp not null default current_timestamp,

    constraint fk_analysis_jobs_document foreign key (document_id) references documents(id)
);

create index idx_analysis_jobs_document_id on analysis_jobs(document_id);
create index idx_analysis_jobs_status on analysis_jobs(status);
